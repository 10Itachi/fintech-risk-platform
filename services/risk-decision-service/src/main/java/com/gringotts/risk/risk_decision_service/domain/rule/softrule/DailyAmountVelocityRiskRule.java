package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DailyAmountVelocityRiskRule implements SoftRule {

    @Override
    public int score(RiskDecisionContext ctx) {

        BigDecimal total = ctx.getRequest().getTotalAmountLast24h();
        if (total == null) return 0;

        if (total.compareTo(BigDecimal.valueOf(500_000)) >= 0) return 50;
        if (total.compareTo(BigDecimal.valueOf(200_000)) >= 0) return 25;

        return 0;
    }

    @Override
    public String code() {
        return "DAILY_AMOUNT_SPIKE";
    }
}
