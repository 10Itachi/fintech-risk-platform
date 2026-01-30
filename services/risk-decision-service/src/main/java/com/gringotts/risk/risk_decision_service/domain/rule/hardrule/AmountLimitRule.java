package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;


import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class AmountLimitRule implements HardRule {
    private final BigDecimal maxAmount;

    public AmountLimitRule(RiskPolicyProperties prop) {
        this.maxAmount = prop.getMaxSingleAmount();
    }

    @Override
    public boolean matches(RiskDecisionContext context) {
        return context.getRequest()
                .getAmount()
                .compareTo(maxAmount) > 0;
    }

    @Override
    public String reasonCode() {
        return "AMOUNT_LIMIT_EXCEEDED";
    }
}
