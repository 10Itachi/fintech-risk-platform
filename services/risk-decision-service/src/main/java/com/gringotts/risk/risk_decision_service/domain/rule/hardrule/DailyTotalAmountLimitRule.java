package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DailyTotalAmountLimitRule implements HardRule{

    private final BigDecimal dailyLimit;

    public DailyTotalAmountLimitRule(RiskPolicyProperties props) {
        this.dailyLimit = props.getMaxDailyAmount();
    }

    @Override
    public boolean matches(RiskDecisionContext ctx) {
        BigDecimal total = ctx.getRequest().getTotalAmountLast24h();
        return total != null && total.compareTo(dailyLimit) > 0;
    }

    @Override
    public String reasonCode() {
        return "DAILY_AMOUNT_LIMIT_EXCEEDED";
    }
}
