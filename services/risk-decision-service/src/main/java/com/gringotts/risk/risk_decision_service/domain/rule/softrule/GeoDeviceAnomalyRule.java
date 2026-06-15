package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

@Component
public class GeoDeviceAnomalyRule implements SoftRule {

    private final RiskPolicyProperties props;

    public GeoDeviceAnomalyRule(RiskPolicyProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RiskDecisionContext ctx) {

        var r = ctx.getRequest();
        var f = ctx.getFeatures();
        var soft = props.getSoftRules();

        if (f.isNewDevice() && !"IN".equalsIgnoreCase(r.getCountry())) {

            ctx.adjustRisk(soft.getDeltas().getMedium());
            ctx.addSoftReason("GEO_DEVICE_ANOMALY");
        }
    }
}
