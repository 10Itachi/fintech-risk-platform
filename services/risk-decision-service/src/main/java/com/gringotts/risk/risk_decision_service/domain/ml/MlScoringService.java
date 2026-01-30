package com.gringotts.risk.risk_decision_service.domain.ml;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class MlScoringService {

    private final LogisticRiskModel riskModel;

    public MlScoringService(LogisticRiskModel riskModel) {
        this.riskModel = riskModel;
    }

    public void evaluate(RiskDecisionContext ctx) {
        double probability = riskModel.score(ctx);
        ctx.setMlProbability(probability);
    }
}


