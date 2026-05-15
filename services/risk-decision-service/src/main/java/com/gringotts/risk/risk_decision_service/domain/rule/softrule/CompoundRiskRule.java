package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

@Component
public class CompoundRiskRule implements SoftRule {

    private final RiskPolicyProperties props;

    public CompoundRiskRule(RiskPolicyProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RiskDecisionContext ctx) {

        var r = ctx.getRequest();
        var f = ctx.getFeatures();
        var soft = props.getSoftRules();

        int signals = 0;

        if (f.isNewDevice()) signals++;
        if (f.isOddHour()) signals++;
        if (r.getAmount().doubleValue() > soft.getMediumAmount()) signals++;

        if (signals >= 3) {
            ctx.adjustRisk(soft.getDeltas().getCritical());
            ctx.addSoftReason("COMPOUND_RISK");
        }
    }
}
