package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InvalidAmountRule implements HardRule{


    @Override
    public boolean matches(RiskDecisionContext context) {
        return context.getRequest()
                .getAmount()
                .compareTo(BigDecimal.ZERO)<=0;
    }

    @Override
    public String reasonCode() {
        return "INVALID_AMOUNT";
    }
}
