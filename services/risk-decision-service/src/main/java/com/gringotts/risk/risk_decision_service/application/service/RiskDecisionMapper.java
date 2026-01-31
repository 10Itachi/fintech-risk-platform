package com.gringotts.risk.risk_decision_service.application.service;

import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.ml.ModelMetadata;
import org.springframework.stereotype.Component;

@Component
public class RiskDecisionMapper {

    public RiskDecisionResponse toResponse(RiskDecisionContext ctx) {
        ModelMetadata metadata = ctx.getModelMetadata();
        return RiskDecisionResponse.builder()
                .transactionStatus(ctx.getFinalStatus())
                .riskScore(ctx.getSoftScore())
                .reasonCode(ctx.getReasonCodes())
                .fraudProbability(ctx.getMlProbability())
                .modelName(metadata!=null? metadata.getModelName() : null)
                .modelVersion(metadata!=null? metadata.getModelVersion() : null)
                .trainedAt(metadata!=null? metadata.getTrainedAt() : null)
                .policyVersion(String.valueOf(ctx.getPolicyVersion()))
                .build();
    }
}
