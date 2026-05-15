package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.transaction.transaction_service.infrastructure.feignclient.RiskServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RiskEvaluationService {

    private final RiskServiceClient riskServiceClient;

    private final MeterRegistry meterRegistry;

    public RiskEvaluationService(
            RiskServiceClient riskServiceClient,
            MeterRegistry meterRegistry
    ) {

        this.riskServiceClient = riskServiceClient;
        this.meterRegistry = meterRegistry;
    }

    /*
     =========================================================
     EXTERNAL RISK SERVICE CALL
     =========================================================

     Resilience protections:
     - Retry
     - Circuit breaker
     - Timeout protection
     - Controlled fallback
     */
    @Retry(name = "riskService")
    @CircuitBreaker(
            name = "riskService",
            fallbackMethod = "riskFallback"
    )
    public RiskDecisionResponse callRiskService(RiskDecisionRequest request) {
        Timer.Sample timer = Timer.start(meterRegistry);

        try {
            RiskDecisionResponse response = riskServiceClient.evaluateRisk(request);
            meterRegistry.counter("risk.service.success").increment();
            return response;

        } finally {
            timer.stop(meterRegistry.timer("risk.service.latency")
            );
        }
    }

    /*
     =========================================================
     FALLBACK STRATEGY
     =========================================================

     Fintech fail-open strategy:
     If risk system unavailable,
     send transaction for manual review.
     */
    public RiskDecisionResponse riskFallback(
            RiskDecisionRequest request,
            Throwable ex
    ) {

        meterRegistry.counter(
                "risk.service.fallback"
        ).increment();

        String exceptionName =
                (ex != null)
                        ? ex.getClass().getSimpleName()
                        : "UnknownException";

        String exceptionMessage =
                (ex != null)
                        ? ex.getMessage()
                        : "No exception message available";

        log.error(
                "risk_service_fallback txnId={} exception={} message={}",
                request.getTransactionId(),
                exceptionName,
                exceptionMessage
        );

        return RiskDecisionResponse.reviewFallback();
    }
}
