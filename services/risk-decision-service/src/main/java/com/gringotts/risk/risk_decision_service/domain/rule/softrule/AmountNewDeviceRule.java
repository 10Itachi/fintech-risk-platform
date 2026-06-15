package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

@Component
public class AmountNewDeviceRule implements SoftRule {

    private final RiskPolicyProperties props;

    public AmountNewDeviceRule(RiskPolicyProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RiskDecisionContext ctx) {

        var r = ctx.getRequest();
        var f = ctx.getFeatures();
        var soft = props.getSoftRules();

        if (r.getAmount().doubleValue() > soft.getMediumAmount()
                && f.isNewDevice()) {

            ctx.adjustRisk(soft.getDeltas().getMedium());
            ctx.addSoftReason("AMOUNT_NEW_DEVICE");
        }
    }
}