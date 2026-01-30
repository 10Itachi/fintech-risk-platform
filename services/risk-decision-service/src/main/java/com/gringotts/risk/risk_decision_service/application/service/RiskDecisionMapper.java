package com.gringotts.risk.risk_decision_service.application.service;

import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class RiskDecisionMapper {

    public RiskDecisionResponse toResponse(RiskDecisionContext ctx) {

        return RiskDecisionResponse.builder()
                .transactionStatus(ctx.getFinalStatus())
                .riskScore(ctx.getSoftScore())
                .fraudProbability(ctx.getMlProbability())
                .reasonCode(ctx.getReasonCodes())
                .build();
    }
}
