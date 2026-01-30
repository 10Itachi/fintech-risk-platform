package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.enums.Channel;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class ChannelRiskRule implements SoftRule {

    @Override
    public int score(RiskDecisionContext ctx) {
        return ctx.getRequest().getChannel() == Channel.CARD ? 15 : 0;
    }

    @Override
    public String code() {
        return "RISKY_CHANNEL";
    }
}
