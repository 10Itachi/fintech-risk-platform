package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;

public interface HardRule {
    boolean matches(RiskDecisionContext context);
    String reasonCode();
}
