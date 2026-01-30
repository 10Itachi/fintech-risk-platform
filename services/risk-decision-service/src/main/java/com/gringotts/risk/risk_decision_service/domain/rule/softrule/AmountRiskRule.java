package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmountRiskRule implements SoftRule {

    @Override
    public int score(RiskDecisionContext ctx) {
        BigDecimal amount = ctx.getRequest().getAmount();

        if (amount.compareTo(BigDecimal.valueOf(100_000)) >= 0) return 40;
        if (amount.compareTo(BigDecimal.valueOf(25_000)) >= 0) return 20;

        return 0;
    }

    @Override
    public String code() {
        return "HIGH_AMOUNT";
    }
}