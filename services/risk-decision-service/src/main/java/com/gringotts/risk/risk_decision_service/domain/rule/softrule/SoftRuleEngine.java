package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SoftRuleEngine {

    private final List<SoftRule> rules;

    public SoftRuleEngine(List<SoftRule> rules) {
        this.rules = rules;
    }

    public void evaluate(RiskDecisionContext ctx) {

        if (ctx == null) {
            throw new IllegalArgumentException("RiskDecisionContext cannot be null");
        }

        if (ctx.getFeatures() == null) {
            throw new IllegalStateException("Derived features must be attached before soft rule evaluation");
        }

        for (SoftRule rule : rules) {
            rule.apply(ctx);
        }
    }
}
