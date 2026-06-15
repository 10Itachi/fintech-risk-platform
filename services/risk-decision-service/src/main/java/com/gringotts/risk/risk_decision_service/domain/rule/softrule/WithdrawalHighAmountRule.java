package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.enums.TransactionType;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalHighAmountRule implements SoftRule {

    private final RiskPolicyProperties props;

    public WithdrawalHighAmountRule(RiskPolicyProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RiskDecisionContext ctx) {

        var r = ctx.getRequest();
        var soft = props.getSoftRules();

        if (r.getTransactionType() == TransactionType.WITHDRAWAL &&
                r.getAmount().doubleValue() > soft.getHighAmount()) {

            ctx.adjustRisk(soft.getDeltas().getHigh());
            ctx.addSoftReason("WITHDRAWAL_HIGH_AMOUNT");
        }
    }
}