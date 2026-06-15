package com.gringotts.risk.risk_decision_service.domain.decision;

import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.risk.risk_decision_service.domain.ml.ModelMetadata;
import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import org.springframework.stereotype.Component;

@Component
public  class RiskDecisionMapper {

    // build RiskDecisionResponse using RiskDecisionContext
    public RiskDecisionResponse toResponse(RiskDecisionContext ctx) {
        ModelMetadata metadata = ctx.getModelMetadata();

        return RiskDecisionResponse.builder()
                .transactionStatus(ctx.getFinalStatus())
                //.riskScore(ctx.getSoftScore())
                .reasonCodes(ctx.getReasonCodes())
                .fraudProbability(ctx.getMlProbability())
                .modelName(metadata != null ? metadata.getModelName() : null)
                .modelVersion(metadata != null ? metadata.getModelVersion() : null)
                .trainedAt(metadata != null ? metadata.getTrainedAt() : null)
                .policyVersion(String.valueOf(ctx.getPolicyVersion()))
                .build();
    }

    // build RiskDecisionResponse using existing RiskDecisionTraceEntity from db
    public RiskDecisionResponse fromEntity(RiskDecisionTraceEntity entity) {

        return RiskDecisionResponse.builder()
                .transactionStatus(entity.getFinalStatus())
                //.riskScore(entity.getSoftRiskScore())
                .reasonCodes(entity.getReasonCodes())
                .fraudProbability(entity.getMlProbability())
                .modelName(entity.getModelName())
                .modelVersion(entity.getModelVersion())
                .trainedAt(entity.getTrainedAt())
                .policyVersion(entity.getPolicyVersion())
                .build();
    }

}
