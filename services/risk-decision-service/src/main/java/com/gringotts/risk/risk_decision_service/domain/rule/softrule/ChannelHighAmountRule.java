package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.enums.Channel;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

@Component
public class ChannelHighAmountRule implements SoftRule {

    private final RiskPolicyProperties props;

    public ChannelHighAmountRule(RiskPolicyProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RiskDecisionContext ctx) {

        var r = ctx.getRequest();
        var soft = props.getSoftRules();

        if (r.getChannel() == Channel.CARD &&
                r.getAmount().doubleValue() > soft.getCardHighAmount()) {

            ctx.adjustRisk(soft.getDeltas().getMedium());
            ctx.addSoftReason("CHANNEL_HIGH_AMOUNT");
        }
    }
}
