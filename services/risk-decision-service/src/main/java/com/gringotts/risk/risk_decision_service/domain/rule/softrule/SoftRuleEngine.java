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
        int total = 0;

        for (SoftRule rule : rules) {
            int score = rule.score(ctx);
            if (score > 0) {
                ctx.addSoftReason(rule.code());
                total += score;
            }
        }

        ctx.setSoftScore(total);
    }

}
