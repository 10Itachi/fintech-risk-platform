package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class NewDeviceRiskRule implements SoftRule {

    @Override
    public int score(RiskDecisionContext ctx) {
        return ctx.isNewDevice() ? 20 : 0;
    }

    @Override
    public String code() {
        return "NEW_DEVICE";
    }
}
