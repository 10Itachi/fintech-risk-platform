package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class HardRuleEngine {
    private final List<HardRule> rules;

    public HardRuleEngine(List<HardRule> rules) {
        this.rules = rules;
    }

    public List<String> evaluate(RiskDecisionContext ctx) {
        List<String> reasons = new ArrayList<>();
        for (HardRule rule : rules) {
            if (rule.matches(ctx)) {
                // risk decision context call
                reasons.add(rule.reasonCode());
                ctx.triggerHardFail(rule.reasonCode());
            }
        }
        return reasons;
    }
}
