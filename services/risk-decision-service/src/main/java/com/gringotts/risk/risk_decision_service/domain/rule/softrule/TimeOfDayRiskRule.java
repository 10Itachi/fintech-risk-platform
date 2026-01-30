package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class TimeOfDayRiskRule implements SoftRule {

    @Override
    public int score(RiskDecisionContext ctx) {
        int hour = ctx.getRequest().getTransactionTime()
                .atZone(ZoneOffset.UTC)
                .getHour();

        return (hour >= 0 && hour <= 4) ? 10 : 0;
    }

    @Override
    public String code() {
        return "ODD_HOUR_TRANSACTION";
    }
}
