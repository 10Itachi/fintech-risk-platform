package com.gringotts.risk.risk_decision_service.application;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.risk.risk_decision_service.AdminDto.RiskDecisionAdminResponse;
import com.gringotts.risk.risk_decision_service.AdminDto.RiskDecisionTraceMapper;
import com.gringotts.risk.risk_decision_service.domain.DerivedRiskFeature.DerivedRiskFeatureBuilder;
import com.gringotts.risk.risk_decision_service.domain.decision.*;
import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import com.gringotts.risk.risk_decision_service.domain.rule.hardrule.HardRuleEngine;
import com.gringotts.risk.risk_decision_service.enums.RiskFailureType;
import com.gringotts.risk.risk_decision_service.exception.DuplicateTransactionInProgressException;
import com.gringotts.risk.risk_decision_service.exception.RiskDecisionNotFoundException;
import com.gringotts.risk.risk_decision_service.exception.RiskEvaluationException;
import com.gringotts.risk.risk_decision_service.infrastructure.repository.RiskDecisionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EnableRetry
@Service
public class RiskDecisionApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskDecisionApplicationService.class);

    private final HardRuleEngine hardRuleEngine;
    private final RiskDecisionPolicy decisionPolicy;
    private final RiskDecisionMapper mapper;
    private final RiskDecisionRepository riskDecisionRepository;
    private final RiskDecisionTraceMapper traceMapper;
    private final MeterRegistry meterRegistry;
    private final RiskDecisionContextFactory decisionContextFactory;
    private final DerivedRiskFeatureBuilder derivedRiskFeatureBuilder;
    private final RedisIdempotencyService idempotencyService;
    private final ResilientMlScoringService resilientMlScoringService;
    private final ResilientSoftRuleEngine resilientSoftRuleEngine;

    public RiskDecisionApplicationService(
            HardRuleEngine hardRuleEngine,
            RiskDecisionPolicy decisionPolicy,
            RiskDecisionMapper mapper,
            RiskDecisionRepository riskDecisionRepository,
            RiskDecisionTraceMapper traceMapper,
            MeterRegistry meterRegistry,
            RiskDecisionContextFactory decisionContextFactory,
            DerivedRiskFeatureBuilder derivedRiskFeatureBuilder,
            RedisIdempotencyService idempotencyService,
            ResilientMlScoringService resilientMlScoringService,
            ResilientSoftRuleEngine resilientSoftRuleEngine) {

        this.hardRuleEngine = hardRuleEngine;
        this.decisionPolicy = decisionPolicy;
        this.mapper = mapper;
        this.riskDecisionRepository = riskDecisionRepository;
        this.traceMapper = traceMapper;
        this.meterRegistry = meterRegistry;
        this.decisionContextFactory = decisionContextFactory;
        this.derivedRiskFeatureBuilder = derivedRiskFeatureBuilder;
        this.idempotencyService = idempotencyService;
        this.resilientMlScoringService = resilientMlScoringService;
        this.resilientSoftRuleEngine = resilientSoftRuleEngine;
    }

    /**
     * CORE FLOW - Idempotent + Observable + Resilient
     */
    public RiskDecisionResponse evaluate(RiskDecisionRequest request) {

        final String transactionId = String.valueOf(request.getTransactionId());

        meterRegistry.counter("risk.decision.requests").increment();
        Timer.Sample totalTimer = Timer.start(meterRegistry);

        LOGGER.info("risk_decision_started transactionId={}", transactionId);

        boolean isNew;

        /*
     IDEMPOTENCY LOCK ACQUISITION
     Redis is optimization layer.
     DB remains authoritative source of truth.

     If Redis fails:
     - continue processing
     - DB unique constraint still protects duplicates
     */
        try {
            isNew = idempotencyService.tryLock(transactionId);
        } catch (Exception ex) {
            meterRegistry.counter("risk.redis.failure", "operation", "lock").increment();
            LOGGER.error("redis_lock_failure transactionId={}", transactionId, ex);
            /*
         Continue processing.
         DB unique constraint acts as final safety net.
         */
            isNew = true;
        }

        /*
     DUPLICATE REQUEST FLOW
     */
        if (!isNew) {
            meterRegistry.counter("risk.decision.duplicate").increment();
            LOGGER.warn("duplicate_request transactionId={}", transactionId);
            /*
         Try Redis completed state first.
         This avoids unnecessary DB hit for completed requests.
         */
            try {
                Optional<String> decisionIdOpt =
                        idempotencyService.getCompletedDecisionId(transactionId);
                if (decisionIdOpt.isPresent()) {
                    Long decisionId = Long.valueOf(decisionIdOpt.get());

                    return riskDecisionRepository.findById(decisionId)
                            .map(mapper::fromEntity)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Decision exists in Redis but missing in DB"
                                    ));
                }
            } catch (Exception ex) {
                meterRegistry.counter("risk.redis.failure", "operation", "read").increment();
                LOGGER.error("redis_read_failure transactionId={}", transactionId, ex);
            }

/*
         DB AUTHORITATIVE FALLBACK
         Handles:
         - Redis inconsistency window
         - Redis write failure
         - Redis downtime
         - concurrent request race conditions
         */
            Optional<RiskDecisionResponse> existingDecision =
                    riskDecisionRepository.findByTransactionId(transactionId)
                            .map(mapper::fromEntity);

            if (existingDecision.isPresent()) {

                LOGGER.info(
                        "duplicate_resolved_from_db transactionId={}",
                        transactionId
                );

                return existingDecision.get();
            }
              /*
         STILL PROCESSING
         Do NOT block servlet thread using Thread.sleep().
         Return controlled response immediately.
         */
            LOGGER.warn(
                    "transaction_still_processing transactionId={}",
                    transactionId
            );

            throw new DuplicateTransactionInProgressException(
                    "Transaction is currently being processed"
            );
        }

        // new transaction
        try {
            try {
                idempotencyService.isInProgress(transactionId);
            } catch (Exception ex) {
                meterRegistry.counter("risk.redis.failure", "operation", "mark").increment();
                LOGGER.error("redis_mark_failed transactionId={}", transactionId, ex);
            }

            // build RiskDecisionContext ctx and attached derived features
            RiskDecisionContext ctx = decisionContextFactory.create(request);
            ctx.attachDerivedFeatures(derivedRiskFeatureBuilder.build(ctx));

            // apply hard rules
            List<String> reasons = hardRuleEngine.evaluate(ctx);
            if (!reasons.isEmpty()) {
                LOGGER.info("hard_rule_triggered transactionId={} reasons={}", transactionId, reasons);
            }

            if (!ctx.isDeclined()) {

                // call ml for ml-score
                Timer.Sample mlTimer = Timer.start(meterRegistry);
                resilientMlScoringService.evaluate(ctx);
                mlTimer.stop(meterRegistry.timer("risk.ml.latency"));

                //call soft rules for scoring
                Timer.Sample softTimer = Timer.start(meterRegistry);
                resilientSoftRuleEngine.evaluate(ctx);
                softTimer.stop(meterRegistry.timer("risk.soft.latency"));
            }

            // the final decision using RiskDecisionPolicy
            ctx.setFinalStatus(decisionPolicy.decide(ctx));

            LOGGER.info("decision_completed transactionId={} result={} mlProbability={} reasons={}",
                    transactionId, ctx.getFinalStatus(), ctx.getMlProbability(), ctx.getReasonCodes());

            RiskDecisionTrace trace = new RiskDecisionTrace(
                    ctx.getFinalStatus(),
                    ctx.getMlProbability(),
                    ctx.getReasonCodes(),
                    ctx.evaluationTime(),
                    ctx.getModelMetadata(),
                    ctx.getPolicyVersion()
            );

            RiskDecisionTraceEntity entity;
            Timer.Sample dbTimer = Timer.start(meterRegistry);

            try {
                entity = saveWithRetry(trace, request);
            } catch (DataIntegrityViolationException ex) {
                meterRegistry.counter("risk.db.duplicate").increment();
                LOGGER.warn("db_duplicate transactionId={}", transactionId);

                return riskDecisionRepository.findByTransactionId(transactionId)
                        .map(mapper::fromEntity)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Duplicate exists but fetch failed"
                                ));
            }

            dbTimer.stop(meterRegistry.timer("risk.db.latency"));

            try {
                idempotencyService.markCompleted(transactionId, String.valueOf(entity.getId()));
            } catch (Exception ex) {
                meterRegistry.counter("risk.redis.failure", "operation", "mark").increment();
                LOGGER.error("redis_mark_failed transactionId={}", transactionId, ex);
            }

            meterRegistry.counter("risk.decision.result",
                    "status", ctx.getFinalStatus().name()).increment();

            totalTimer.stop(
                    Timer.builder("risk.decision.latency")
                            .tag("result", ctx.getFinalStatus().name())
                            .register(meterRegistry)
            );
            return mapper.fromEntity(entity);
           // return mapper.toResponse(ctx);

        } catch (RiskEvaluationException ex) {

            meterRegistry.counter("risk.decision.failure",
                    "type", ex.getFailureType().name()).increment();

            LOGGER.error("evaluation_failed transactionId={} type={}",
                    transactionId, ex.getFailureType(), ex);

            idempotencyService.releaseLock(transactionId);

            return buildFailureResponse(ex.getFailureType());

        } catch (Exception ex) {

            meterRegistry.counter("risk.decision.failure",
                    "type", "UNKNOWN").increment();

            LOGGER.error("unexpected_failure transactionId={}", transactionId, ex);

            idempotencyService.releaseLock(transactionId);

            return buildFailureResponse(RiskFailureType.UNKNOWN_FAILURE);
        }
    }

    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 3))
    public RiskDecisionTraceEntity saveWithRetry(RiskDecisionTrace trace, RiskDecisionRequest request) {

        String transactionId = String.valueOf(request.getTransactionId());

        try {
            RiskDecisionTraceEntity entity = traceMapper.toEntity(trace, request);
            RiskDecisionTraceEntity saved = riskDecisionRepository.save(entity);

            LOGGER.debug("trace_persisted transactionId={} decisionId={}",
                    transactionId, saved.getId());

            return saved;

        } catch (Exception ex) {
            LOGGER.error("db_save_failed transactionId={}", transactionId, ex);
            throw ex;
        }
    }

    private RiskDecisionResponse buildFailureResponse(RiskFailureType failureType) {

        RiskDecisionResponse base = RiskDecisionResponse.reviewFallback();

        List<String> updatedReasons = new ArrayList<>(base.getReasonCodes());
        updatedReasons.add("FAILURE_TYPE_" + failureType.name());

        return RiskDecisionResponse.builder()
                .transactionStatus(base.getTransactionStatus())
                .riskScore(base.getRiskScore())
                .fraudProbability(base.getFraudProbability())
                .policyVersion(base.getPolicyVersion())
                .modelName(base.getModelName())
                .modelVersion(base.getModelVersion())
                .trainedAt(base.getTrainedAt())
                .reasonCodes(updatedReasons)
                .build();
    }


    /**
     * ADMIN APIs
     */
    @Cacheable(value = "decisionById", key = "#decisionId")
    @Transactional(readOnly = true)
    public RiskDecisionAdminResponse getById(Long decisionId) {

        meterRegistry.counter("risk.admin.request", "endpoint", "getById").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            LOGGER.info("admin_getById_start decisionId={}", decisionId);

            RiskDecisionTraceEntity entity = riskDecisionRepository.findById(decisionId)
                    .orElseThrow(() -> new RiskDecisionNotFoundException(
                            "Risk decision not found for id: " + decisionId));

            LOGGER.debug("admin_getById_success decisionId={}", decisionId);

            return traceMapper.toAdminResponse(entity);

        } catch (Exception ex) {

            meterRegistry.counter("risk.admin.failure",
                    "endpoint", "getById").increment();

            LOGGER.error("admin_getById_failed decisionId={}", decisionId, ex);
            throw ex;

        } finally {
            sample.stop(
                    Timer.builder("risk.admin.latency")
                            .tag("endpoint", "getById")
                            .register(meterRegistry)
            );
        }
    }

    @Cacheable(value = "decisionByTransactionId", key = "#transactionId")
    @Transactional(readOnly = true)
    public RiskDecisionAdminResponse getDecisionByTransactionId(String transactionId) {

        meterRegistry.counter("risk.admin.request", "endpoint", "getByTransactionId").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            LOGGER.info("admin_getByTxn_start transactionId={}", transactionId);

            RiskDecisionTraceEntity entity = riskDecisionRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new RiskDecisionNotFoundException(
                            "Risk decision not found for transactionId: " + transactionId));

            LOGGER.debug("admin_getByTxn_success transactionId={}", transactionId);

            return traceMapper.toAdminResponse(entity);

        } catch (Exception ex) {

            meterRegistry.counter("risk.admin.failure",
                    "endpoint", "getByTransactionId").increment();

            LOGGER.error("admin_getByTxn_failed transactionId={}", transactionId, ex);
            throw ex;

        } finally {
            sample.stop(
                    Timer.builder("risk.admin.latency")
                            .tag("endpoint", "getByTransactionId")
                            .register(meterRegistry)
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<RiskDecisionAdminResponse> getAllDecisions(
            int page,
            int size,
            Instant from,
            Instant to) {

        meterRegistry.counter("risk.admin.request", "endpoint", "getAll").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            LOGGER.info("admin_getAll_start page={} size={} from={} to={}",
                    page, size, from, to);

            Pageable pageable = PageRequest.of(page, size, Sort.by("evaluatedAt").descending());

            Page<RiskDecisionTraceEntity> entityPage;

            if (from != null && to != null) {

                entityPage = riskDecisionRepository
                        .findByEvaluatedAtBetween(from, to, pageable);

            } else {

                entityPage = riskDecisionRepository.findAll(pageable);
            }

            LOGGER.debug("admin_getAll_success page={} size={}", page, size);

            return entityPage.map(traceMapper::toAdminResponse);

        } catch (Exception ex) {

            meterRegistry.counter("risk.admin.failure",
                    "endpoint", "getAll").increment();

            LOGGER.error("admin_getAll_failed page={} size={}", page, size, ex);
            throw ex;

        } finally {
            sample.stop(
                    Timer.builder("risk.admin.latency")
                            .tag("endpoint", "getAll")
                            .register(meterRegistry)
            );
        }
    }
}




