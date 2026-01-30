package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class InvalidTransactionTimeRule implements HardRule {

    private final RiskPolicyProperties props;

    public InvalidTransactionTimeRule(RiskPolicyProperties props) {
        this.props = props;
    }

    @Override
    public boolean matches(RiskDecisionContext ctx) {
        Instant txnTime = ctx.getRequest().getTransactionTime();
        Instant now = ctx.evaluationTime();

        return txnTime.isAfter(now.plus(props.getMaxFutureDrift()))
                || txnTime.isBefore(now.minus(props.getMaxPastDrift()));
    }

    @Override
    public String reasonCode() {
        return "INVALID_TRANSACTION_TIME";
    }
}

