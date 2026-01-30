package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;

public interface SoftRule {
    int score(RiskDecisionContext ctx);
    String code();
}