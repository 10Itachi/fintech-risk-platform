package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class HardRuleEngine {
    private final List<HardRule> rules;

    public HardRuleEngine(List<HardRule> rules) {
        this.rules = rules;
    }

    public Optional<String> evaluate(RiskDecisionContext ctx) {

        for (HardRule rule : rules) {
            if (rule.matches(ctx)) {
                return Optional.of(rule.reasonCode());
            }
        }
        return Optional.empty();
    }
}
