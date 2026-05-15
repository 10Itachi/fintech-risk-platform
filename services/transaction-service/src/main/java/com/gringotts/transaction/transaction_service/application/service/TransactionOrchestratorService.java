package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.transaction.transaction_service.Utils.RequestMetadataUtils;
import com.gringotts.transaction.transaction_service.config.security.SecurityUtils;
import com.gringotts.transaction.transaction_service.domain.businessEnums.InternalTransactionStatus;
import com.gringotts.transaction.transaction_service.domain.exception.InvalidTransactionException;
import com.gringotts.transaction.transaction_service.domain.exception.TransactionInProgressException;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.dto.request.TransactionRequestDto;
import com.gringotts.transaction.transaction_service.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.infrastructure.redis.RedisIdempotencyService;
import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import com.gringotts.transaction.transaction_service.mapper.RiskRequestMapper;
import com.gringotts.transaction.transaction_service.mapper.TransactionMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TransactionOrchestratorService {

    private final TransactionCommandService commandService;

    private final RiskEvaluationService riskService;

    private final RiskRequestMapper riskRequestMapper;

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final TransactionRiskFeatureService riskFeatureService;

    private final TransactionBusinessValidator businessValidator;

    private final MeterRegistry meterRegistry;

    private final RedisIdempotencyService redisIdempotencyService;

    public TransactionOrchestratorService(
            TransactionCommandService commandService,
            RiskEvaluationService riskService,
            RiskRequestMapper riskRequestMapper,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            TransactionRiskFeatureService riskFeatureService,
            TransactionBusinessValidator businessValidator,
            MeterRegistry meterRegistry,
            RedisIdempotencyService redisIdempotencyService
    ) {

        this.commandService = commandService;
        this.riskService = riskService;
        this.riskRequestMapper = riskRequestMapper;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.riskFeatureService = riskFeatureService;
        this.businessValidator = businessValidator;
        this.meterRegistry = meterRegistry;
        this.redisIdempotencyService = redisIdempotencyService;
    }

    /*
     =========================================================
     MAIN TRANSACTION ORCHESTRATION FLOW
     =========================================================

     Responsibilities:
     - idempotency coordination
     - duplicate handling
     - workflow orchestration
     - external risk coordination
     - response orchestration

     NOT responsible for:
     - transactional persistence logic
     - lifecycle state machine logic
     - DB consistency rules
     */
    public TransactionResponseDto createTransaction(
            TransactionRequestDto request,
            String idempotencyKey
    ) {

        meterRegistry.counter("transaction.create.request")
                .increment();

        Timer.Sample totalTimer =
                Timer.start(meterRegistry);

        log.info(
                "transaction_creation_started idempotencyKey={}",
                idempotencyKey
        );

        boolean isNewRequest = true;

        /*
         =====================================================
         REDIS IDEMPOTENCY LOCK
         =====================================================

         Redis is optimization layer.
         DB unique constraint remains final safety net.
         */
        try {

            isNewRequest =
                    redisIdempotencyService.tryLock(idempotencyKey);

        } catch (Exception ex) {

            meterRegistry.counter(
                    "transaction.redis.failure",
                    "operation",
                    "lock"
            ).increment();

            log.error(
                    "redis_lock_failure idempotencyKey={}",
                    idempotencyKey,
                    ex
            );

            /*
             Continue processing.

             DB unique constraint still protects duplicates.
             */
            isNewRequest = true;
        }

        /*
         =====================================================
         DUPLICATE REQUEST FLOW
         =====================================================
         */
        if (!isNewRequest) {

            meterRegistry.counter(
                    "transaction.duplicate"
            ).increment();

            log.warn(
                    "duplicate_transaction_request idempotencyKey={}",
                    idempotencyKey
            );

            /*
             -------------------------------------------------
             FAST REDIS LOOKUP
             -------------------------------------------------

             If Redis already contains completed transactionId,
             fetch directly from DB using transactionId.
             */
            try {

                Optional<String> completedTxnId =
                        redisIdempotencyService
                                .getCompletedTransactionId(
                                        idempotencyKey
                                );

                if (completedTxnId.isPresent()) {

                    UUID txnId =
                            UUID.fromString(completedTxnId.get());

                    return transactionRepository.findById(txnId)
                            .map(transactionMapper::toDto)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Transaction exists in Redis but missing in DB"
                                    ));
                }

            } catch (Exception ex) {

                meterRegistry.counter(
                        "transaction.redis.failure",
                        "operation",
                        "read"
                ).increment();

                log.error(
                        "redis_read_failure idempotencyKey={}",
                        idempotencyKey,
                        ex
                );
            }

            /*
             -------------------------------------------------
             DB AUTHORITATIVE FALLBACK
             -------------------------------------------------

             Handles:
             - Redis downtime
             - Redis inconsistency window
             - markCompleted failure
             */
            Optional<Transaction> existingTransaction =
                    transactionRepository.findByIdempotencyKey(
                            idempotencyKey
                    );

            if (existingTransaction.isPresent()) {

                Transaction txn =
                        existingTransaction.get();

                log.info(
                        "duplicate_resolved_from_db idempotencyKey={} txnId={} status={}",
                        idempotencyKey,
                        txn.getTransactionId(),
                        txn.getInternalStatus()
                );

                /*
                 FINAL STATE → SAFE TO RETURN
                 */
                if (isFinalState(txn.getInternalStatus())) {

                    meterRegistry.counter(
                            "transaction.duplicate.resolved"
                    ).increment();

                    return transactionMapper.toDto(txn);
                }
            }

            /*
             -------------------------------------------------
             TRANSACTION STILL PROCESSING
             -------------------------------------------------

             Never block servlet thread using sleep/retry loops.
             */
            meterRegistry.counter(
                    "transaction.in.progress"
            ).increment();

            throw new TransactionInProgressException(
                    "Transaction is currently being processed"
            );
        }

        /*
         =====================================================
         NEW TRANSACTION FLOW
         =====================================================
         */
        try {

            /*
             -------------------------------------------------
             BUSINESS VALIDATION
             -------------------------------------------------
             */
            businessValidator.validate(request);

            /*
             -------------------------------------------------
             BUILD TRANSACTION
             -------------------------------------------------
             */
            Transaction txn =
                    buildTransaction(request, idempotencyKey);

            /*
             -------------------------------------------------
             TX1 → INITIAL PERSIST
             -------------------------------------------------

             Ensures:
             - transactionId exists
             - createdAt exists
             - initial lifecycle state persisted
             */
            txn = commandService.createInitialTransaction(txn);

            /*
             -------------------------------------------------
             FEATURE ENRICHMENT
             -------------------------------------------------
             */
            txn.setTotalAmountLast24h(
                    riskFeatureService.totalAmountLast24H(
                            txn.getUserId(),
                            txn.getCreatedAt()
                    )
            );

            txn.setTxnCountLast24h(
                    riskFeatureService.numberOfTransactionsLast24h(
                            txn.getUserId(),
                            txn.getCreatedAt()
                    )
            );

            /*
             -------------------------------------------------
             BUILD RISK REQUEST
             -------------------------------------------------
             */
            RiskDecisionRequest riskRequest =
                    riskRequestMapper.toRiskRequest(txn);

            /*
             -------------------------------------------------
             RISK SERVICE CALL
             -------------------------------------------------
             */
            Timer.Sample riskTimer =
                    Timer.start(meterRegistry);

            RiskDecisionResponse riskResponse =
                    riskService.callRiskService(riskRequest);

            long riskLatencyMs =
                    riskTimer.stop(
                            meterRegistry.timer(
                                    "transaction.risk.latency"
                            )
                    );

            /*
             -------------------------------------------------
             TX2 → FINALIZE TRANSACTION
             -------------------------------------------------
             */
            TransactionResponseDto finalResponse =
                    commandService.finalizeTransaction(
                            txn,
                            riskResponse,
                            riskLatencyMs
                    );

            /*
             =================================================
             MARK REDIS COMPLETED
             =================================================

             Enables fast duplicate replay handling.
             */
            try {

                redisIdempotencyService.markCompleted(
                        idempotencyKey,
                        txn.getTransactionId().toString()
                );

            } catch (Exception ex) {

                meterRegistry.counter(
                        "transaction.redis.failure",
                        "operation",
                        "mark"
                ).increment();

                log.error(
                        "redis_mark_completed_failure idempotencyKey={} txnId={}",
                        idempotencyKey,
                        txn.getTransactionId(),
                        ex
                );
            }

            /*
             =================================================
             SUCCESS METRICS
             =================================================
             */
            meterRegistry.counter(
                    "transaction.completed",
                    "status",
                    finalResponse.getTransactionStatus().name()
            ).increment();

            totalTimer.stop(
                    meterRegistry.timer(
                            "transaction.orchestration.latency"
                    )
            );

            return finalResponse;

        } catch (Exception ex) {

            /*
             =================================================
             FAILURE RECOVERY
             =================================================

             Release IN_PROGRESS lock so request can retry.
             */
            redisIdempotencyService.releaseLock(
                    idempotencyKey
            );

            meterRegistry.counter(
                    "transaction.failure"
            ).increment();

            log.error(
                    "transaction_creation_failed idempotencyKey={}",
                    idempotencyKey,
                    ex
            );

            throw ex;
        }
    }

    /*
     =========================================================
     FINAL STATE CHECK
     =========================================================
     */
    private boolean isFinalState(
            InternalTransactionStatus status
    ) {

        return status == InternalTransactionStatus.RISK_APPROVED
                || status == InternalTransactionStatus.RISK_REJECTED
                || status == InternalTransactionStatus.FAILED;
    }

    /*
     =========================================================
     BUILD TRANSACTION AGGREGATE
     =========================================================
     */
    private Transaction buildTransaction(TransactionRequestDto request, String idempotencyKey) {

        UUID userId = SecurityUtils.getUserId();

        if (userId == null) {
            throw new IllegalArgumentException(
                    "UserId is null or blank"
            );
        }

        String username = SecurityUtils.getUsername();
        String email =SecurityUtils.getEmail();

        Transaction txn = new Transaction();
        txn.setUserId(userId);
        txn.setUserName(username);
        txn.setEmail(email);
        txn.setAmount(request.getAmount());
        txn.setTransactionType(request.getTransactionType());

        txn.setChannel(request.getChannel());
        txn.setSourceAccount(request.getSourceAccount());

        txn.setTargetAccount(request.getTargetAccount());

        txn.setDeviceId(
                RequestMetadataUtils.getDeviceId(
                txn.getChannel())
        );

        txn.setCountry(RequestMetadataUtils.getCountry());
        txn.setTransactionTime(request.getTransactionTime());
        txn.setInternalStatus(InternalTransactionStatus.INITIATED);
        txn.setTransactionStatus(TransactionStatus.INITIATED);
        txn.setIdempotencyKey(idempotencyKey);

        return txn;
    }
}