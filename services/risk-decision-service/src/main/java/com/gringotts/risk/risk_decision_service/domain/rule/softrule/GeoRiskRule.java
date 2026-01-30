package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GeoRiskRule implements SoftRule {

    private static final Set<String> HIGH_RISK = Set.of("SG,HK");

    @Override
    public int score(RiskDecisionContext ctx) {
        return HIGH_RISK.contains(ctx.getRequest().getCountry()) ? 20 : 0;
    }

    @Override
    public String code() {
        return "GEO_RISK";
    }
}
