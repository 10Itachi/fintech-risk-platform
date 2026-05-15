package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

@Component
public class HighVelocityBurstRule implements SoftRule {

    private final RiskPolicyProperties props;

    public HighVelocityBurstRule(RiskPolicyProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RiskDecisionContext ctx) {

        var r = ctx.getRequest();
        var soft = props.getSoftRules();

        if (r.getTxnCountLast24h() != null &&
                r.getTxnCountLast24h() > soft.getVelocityHigh()) {

            ctx.adjustRisk(soft.getDeltas().getHigh());
            ctx.addSoftReason("HIGH_VELOCITY_BURST");
        }
    }
}
