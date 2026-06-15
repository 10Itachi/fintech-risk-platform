package com.gringotts.risk.risk_decision_service.domain.ml;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class MlScoringService {

    private static final Logger log = LoggerFactory.getLogger(MlScoringService.class);

    private final LogisticRiskModel riskModel;

    public MlScoringService(LogisticRiskModel riskModel) {
        this.riskModel = riskModel;
    }

    public void evaluate(RiskDecisionContext ctx) {

        if (ctx == null) {
            throw new IllegalArgumentException("RiskDecisionContext cannot be null");
        }

        long start = System.nanoTime();

        try {
            // stores the calculated result "score"
            double probability = riskModel.score(ctx);

            ctx.setMlProbability(probability);
            ctx.attachModelMetadata(riskModel.getModel().getMetadata());

            long latencyMs = (System.nanoTime() - start) / 1_000_000;

            // AUDIT LOG (ML)
            log.info(
                    "event=ml_scoring txnId={} prob={} latencyMs={} modelVersion={}",
                    ctx.getRequest().getTransactionId(),
                    probability,
                    latencyMs,
                    riskModel.getModel().getMetadata().getModelVersion()
            );
        } catch (Exception e) {

            long latencyMs = (System.nanoTime() - start) / 1_000_000;

            log.error(
                    "event=ml_scoring_failed txnId={} latencyMs={} error={}",
                    ctx.getRequest().getTransactionId(),
                    latencyMs,
                    e.getMessage(),
                    e
            );
            throw new IllegalStateException("ML scoring failed", e);
        }
    }
}