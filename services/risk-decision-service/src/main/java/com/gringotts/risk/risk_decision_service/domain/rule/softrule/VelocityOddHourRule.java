package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

@Component
public class VelocityOddHourRule implements SoftRule {

    private final RiskPolicyProperties props;

    public VelocityOddHourRule(RiskPolicyProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RiskDecisionContext ctx) {

        var r = ctx.getRequest();
        var f = ctx.getFeatures();
        var soft = props.getSoftRules();

        if (r.getTxnCountLast24h() != null &&
                r.getTxnCountLast24h() > soft.getVelocityMedium()
                && f.isOddHour()) {

            ctx.adjustRisk(soft.getDeltas().getMedium());
            ctx.addSoftReason("VELOCITY_ODD_HOUR");
        }
    }
}
