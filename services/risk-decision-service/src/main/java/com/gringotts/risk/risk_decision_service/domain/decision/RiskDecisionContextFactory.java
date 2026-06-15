package com.gringotts.risk.risk_decision_service.domain.decision;

import com.gringotts.dto.RiskDecisionRequest;
import org.springframework.stereotype.Component;

/*Used to attach policy properties version and activated while making context*/
@Component
public class RiskDecisionContextFactory {

    private final RiskPolicyProperties policyProperties;

    public RiskDecisionContextFactory(RiskPolicyProperties policyProperties) {
        this.policyProperties = policyProperties;
    }

    public RiskDecisionContext create(RiskDecisionRequest request) {

        RiskDecisionContext ctx = RiskDecisionContext.from(request);

        ctx.attachPolicyInfo(
                policyProperties.getVersion(),
                policyProperties.getActivatedAt()
        );

        return ctx;
    }
}
