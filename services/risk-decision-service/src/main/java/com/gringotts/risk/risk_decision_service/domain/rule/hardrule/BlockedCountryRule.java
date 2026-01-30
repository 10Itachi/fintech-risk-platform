package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BlockedCountryRule implements HardRule {

  private final Set<String> blockedCountries;

    public BlockedCountryRule(RiskPolicyProperties prop) {
        this.blockedCountries = prop.getBlockedCountries();
    }

    @Override
    public boolean matches(RiskDecisionContext context) {
        return blockedCountries.contains(context.getRequest().getCountry());
    }

    @Override
    public String reasonCode() {
        return "BLOCKED_COUNTRY";
    }
}
