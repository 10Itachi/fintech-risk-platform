package com.gringotts.risk.risk_decision_service.domain.model;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionTrace;
import org.springframework.stereotype.Component;

@Component
public class RiskDecisionTraceMapper {

    public RiskDecisionTraceEntity toEntity(
            RiskDecisionTrace trace,
            RiskDecisionRequest request
    ) {
        RiskDecisionTraceEntity entity = new RiskDecisionTraceEntity();

        entity.setTransactionId(request.getTransactionId().toString());
        entity.setFinalStatus(trace.getFinalStatus());
        entity.setSoftRiskScore(trace.getSoftRiskScore());
        entity.setMlProbability(trace.getMlProbability());
        entity.setReasonCodes(trace.getReasonCodes());
        entity.setEvaluatedAt(trace.getEvaluatedAt());

        return entity;
    }
}