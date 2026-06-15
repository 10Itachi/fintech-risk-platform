package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.enums.TransactionType;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class UnsupportedChannelForTransactionRule implements HardRule{

    @Override
    public boolean matches(RiskDecisionContext ctx) {

        RiskDecisionRequest r = ctx.getRequest();

        return switch (r.getChannel()) {
            case CARD ->
                    r.getTransactionType() == TransactionType.DEPOSIT;
            case UPI ->
                    r.getTransactionType() == TransactionType.WITHDRAWAL;
            case NET_BANKING ->
                    false;
        };
    }

    @Override
    public String reasonCode() {
        return "UNSUPPORTED_CHANNEL_FOR_TRANSACTION";
    }
}
