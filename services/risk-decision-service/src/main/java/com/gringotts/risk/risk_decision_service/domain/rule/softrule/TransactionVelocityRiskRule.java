package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class TransactionVelocityRiskRule implements SoftRule {

    @Override
    public int score(RiskDecisionContext ctx) {

        Integer count = ctx.getRequest().getTxnCountLast24h();
        if (count == null) return 0;

        if (count >= 20) return 40;
        if (count >= 10) return 20;

        return 0;
    }

    @Override
    public String code() {
        return "HIGH_TXN_VELOCITY";
    }
}

