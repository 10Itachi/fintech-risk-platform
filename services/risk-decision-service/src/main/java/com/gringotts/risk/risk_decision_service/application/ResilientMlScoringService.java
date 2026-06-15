package com.gringotts.risk.risk_decision_service.application;


import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.ml.MlScoringService;
import com.gringotts.risk.risk_decision_service.enums.RiskFailureType;
import com.gringotts.risk.risk_decision_service.exception.RiskEvaluationException;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class ResilientMlScoringService {

    private final MlScoringService mlScoringService;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final long TIMEOUT_MS = 300;

    public ResilientMlScoringService(MlScoringService mlScoringService) {
        this.mlScoringService = mlScoringService;
    }

    public void evaluate(RiskDecisionContext ctx) {

        Future<?> future = executor.submit(() -> mlScoringService.evaluate(ctx));

        try {
            future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new RiskEvaluationException(RiskFailureType.ML_TIMEOUT, ex);
        } catch (ExecutionException ex) {
            throw new RiskEvaluationException(RiskFailureType.ML_FAILURE, ex.getCause());
        } catch (Exception ex) {
            throw new RiskEvaluationException(RiskFailureType.ML_FAILURE, ex);
        }
    }
}