package com.gringotts.risk.risk_decision_service.application;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.risk.risk_decision_service.AdminDto.RiskDecisionTraceMapper;
import com.gringotts.risk.risk_decision_service.domain.DerivedRiskFeature.DerivedRiskFeatureBuilder;
import com.gringotts.risk.risk_decision_service.domain.DerivedRiskFeature.DerivedRiskFeatures;
import com.gringotts.risk.risk_decision_service.domain.decision.*;
import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import com.gringotts.risk.risk_decision_service.domain.rule.hardrule.HardRuleEngine;
import com.gringotts.risk.risk_decision_service.enums.RiskFailureType;
import com.gringotts.risk.risk_decision_service.exception.RiskEvaluationException;
import com.gringotts.risk.risk_decision_service.infrastructure.repository.RiskDecisionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RiskDecisionApplicationServiceTest {

    @Mock private HardRuleEngine hardRuleEngine;
    @Mock private RiskDecisionPolicy decisionPolicy;
    @Mock private RiskDecisionMapper mapper;
    @Mock private RiskDecisionRepository repository;
    @Mock private RiskDecisionTraceMapper traceMapper;
    @Mock private RiskDecisionContextFactory contextFactory;
    @Mock private DerivedRiskFeatureBuilder featureBuilder;
    @Mock private RedisIdempotencyService idempotency;
    @Mock private ResilientMlScoringService mlService;
    @Mock private ResilientSoftRuleEngine softRule;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private RiskDecisionApplicationService service;

    private RiskDecisionRequest request;
    private RiskDecisionContext context;

    private UUID txnId;
    private String txnIdStr;

    @BeforeEach
    void setup() {

        request = mock(RiskDecisionRequest.class);

        txnId = UUID.randomUUID();
        txnIdStr = txnId.toString();

        when(request.getTransactionId()).thenReturn(txnId);

        context = mock(RiskDecisionContext.class);

        meterRegistry = new SimpleMeterRegistry();

        service = new RiskDecisionApplicationService(
                hardRuleEngine,
                decisionPolicy,
                mapper,
                repository,
                traceMapper,
                meterRegistry,
                contextFactory,
                featureBuilder,
                idempotency,
                mlService,
                softRule
        );
    }

    // =====================================================
    // HAPPY PATH
    // =====================================================

    @Test
    void shouldProcessTransactionSuccessfully() {

        when(idempotency.tryLock(txnIdStr))
                .thenReturn(true);

        when(contextFactory.create(request))
                .thenReturn(context);

        when(featureBuilder.build(context))
                .thenReturn(mock(DerivedRiskFeatures.class));

        when(hardRuleEngine.evaluate(context))
                .thenReturn(List.of());

        when(context.isDeclined())
                .thenReturn(false);

        when(decisionPolicy.decide(context))
                .thenReturn(TransactionStatus.APPROVED);

        when(context.getFinalStatus())
                .thenReturn(TransactionStatus.APPROVED);

        RiskDecisionTraceEntity entity =
                mock(RiskDecisionTraceEntity.class);

        when(entity.getId()).thenReturn(1L);

        when(repository.save(any()))
                .thenReturn(entity);

        RiskDecisionResponse expected =
                mock(RiskDecisionResponse.class);

        // IMPORTANT FIX
        when(mapper.fromEntity(entity))
                .thenReturn(expected);

        RiskDecisionResponse response =
                service.evaluate(request);

        assertThat(response).isNotNull();

        verify(mlService).evaluate(context);
        verify(softRule).evaluate(context);
    }

    // =====================================================
    // HARD RULE SHORT CIRCUIT
    // =====================================================

    @Test
    void shouldSkipMlAndSoftRulesWhenHardRuleTriggers() {

        when(idempotency.tryLock(txnIdStr))
                .thenReturn(true);

        when(contextFactory.create(request))
                .thenReturn(context);

        when(featureBuilder.build(context))
                .thenReturn(mock(DerivedRiskFeatures.class));

        when(hardRuleEngine.evaluate(context))
                .thenReturn(List.of("BLOCKED"));

        when(context.isDeclined())
                .thenReturn(true);

        when(decisionPolicy.decide(context))
                .thenReturn(TransactionStatus.DECLINED);

        when(context.getFinalStatus())
                .thenReturn(TransactionStatus.DECLINED);

        RiskDecisionTraceEntity entity =
                mock(RiskDecisionTraceEntity.class);

        when(entity.getId()).thenReturn(1L);

        when(repository.save(any()))
                .thenReturn(entity);

        when(mapper.fromEntity(entity))
                .thenReturn(mock(RiskDecisionResponse.class));

        service.evaluate(request);

        verify(mlService, never()).evaluate(any());
        verify(softRule, never()).evaluate(any());
    }

    // =====================================================
    // DUPLICATE REQUEST
    // =====================================================

    @Test
    void shouldReturnCachedResponseWhenDuplicateRequest() {

        when(idempotency.tryLock(txnIdStr))
                .thenReturn(false);

        when(idempotency.getCompletedDecisionId(txnIdStr))
                .thenReturn(Optional.of("1"));

        RiskDecisionTraceEntity entity =
                mock(RiskDecisionTraceEntity.class);

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        when(mapper.fromEntity(entity))
                .thenReturn(mock(RiskDecisionResponse.class));

        RiskDecisionResponse response =
                service.evaluate(request);

        assertThat(response).isNotNull();
    }

    // =====================================================
    // DB DUPLICATE
    // =====================================================

    @Test
    void shouldHandleDbDuplicateGracefully() {

        when(idempotency.tryLock(txnIdStr))
                .thenReturn(true);

        when(contextFactory.create(request))
                .thenReturn(context);

        when(featureBuilder.build(context))
                .thenReturn(mock(DerivedRiskFeatures.class));

        when(hardRuleEngine.evaluate(context))
                .thenReturn(List.of());

        when(context.isDeclined())
                .thenReturn(false);

        when(decisionPolicy.decide(context))
                .thenReturn(TransactionStatus.APPROVED);

        when(repository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);

        RiskDecisionTraceEntity entity =
                mock(RiskDecisionTraceEntity.class);

        when(repository.findByTransactionId(txnIdStr))
                .thenReturn(Optional.of(entity));

        when(mapper.fromEntity(entity))
                .thenReturn(mock(RiskDecisionResponse.class));

        RiskDecisionResponse response =
                service.evaluate(request);

        assertThat(response).isNotNull();
    }

    // =====================================================
    // ML FAILURE
    // =====================================================

    @Test
    void shouldReturnFallbackWhenMlFails() {

        when(idempotency.tryLock(txnIdStr))
                .thenReturn(true);

        when(contextFactory.create(request))
                .thenReturn(context);

        when(featureBuilder.build(context))
                .thenReturn(mock(DerivedRiskFeatures.class));

        when(hardRuleEngine.evaluate(context))
                .thenReturn(List.of());

        when(context.isDeclined())
                .thenReturn(false);

        doThrow(new RiskEvaluationException(
                RiskFailureType.ML_FAILURE,
                null
        )).when(mlService).evaluate(context);

        RiskDecisionResponse response =
                service.evaluate(request);

        assertThat(response.getReasonCodes())
                .anyMatch(code -> code.contains("ML_FAILURE"));

        verify(idempotency).releaseLock(txnIdStr);
    }

    // =====================================================
    // UNKNOWN FAILURE
    // =====================================================

    @Test
    void shouldReturnFallbackForUnknownException() {

        when(idempotency.tryLock(txnIdStr))
                .thenReturn(true);

        when(contextFactory.create(request))
                .thenReturn(context);

        when(featureBuilder.build(context))
                .thenThrow(new RuntimeException("boom"));

        RiskDecisionResponse response =
                service.evaluate(request);

        assertThat(response.getReasonCodes())
                .anyMatch(code -> code.contains("UNKNOWN"));

        verify(idempotency).releaseLock(txnIdStr);
    }
}