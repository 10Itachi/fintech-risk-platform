package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionType;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CardWithdrawalLimitRule implements HardRule {

    private final BigDecimal cardLimit;

    public CardWithdrawalLimitRule(RiskPolicyProperties prop ) {
        this.cardLimit = prop.getCardWithdrawalLimit();
    }

    @Override
    public boolean matches(RiskDecisionContext context) {
        RiskDecisionRequest r =  context.getRequest();
        if(r.getChannel()!= Channel.CARD) return false;
        if(r.getTransactionType()!= TransactionType.WITHDRAWAL) return false;

        return r.getAmount().compareTo(cardLimit) >= 0;
    }

    @Override
    public String reasonCode() {
        return "CARD_WITHDRAWAL_LIMIT_EXCEEDED";
    }
}
