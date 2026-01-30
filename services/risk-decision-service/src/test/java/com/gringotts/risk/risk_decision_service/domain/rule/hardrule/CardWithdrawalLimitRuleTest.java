package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;


import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionType;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CardWithdrawalLimitRuleTest {

    private CardWithdrawalLimitRule cardLimitRule(BigDecimal cardLimit) {
        RiskPolicyProperties properties = new RiskPolicyProperties();
        properties.setCardWithdrawalLimit(cardLimit);
        return new CardWithdrawalLimitRule(properties);
    }

    private RiskDecisionContext contet(Channel channel,
                                       TransactionType type,
                                       BigDecimal amount) {
        RiskDecisionRequest request = new RiskDecisionRequest();
        request.setChannel(channel);
        request.setTransactionType(type);
        request.setAmount(amount);

        return RiskDecisionContext.from(request);
    }

    @Test
    void should_not_match_when_channel_is_not_card() {
        CardWithdrawalLimitRule rule = cardLimitRule(new BigDecimal("5000"));
        RiskDecisionContext ctx = contet(Channel.UPI, TransactionType.WITHDRAWAL, new BigDecimal("5000"));
        assertFalse(rule.matches(ctx));
    }

    @Test
    void should_not_match_if_the_return_type_is_not_withdrawal(){
        CardWithdrawalLimitRule rule = cardLimitRule(new BigDecimal("5000"));
        RiskDecisionContext ctx = contet(Channel.CARD, TransactionType.TRANSFER, new BigDecimal("5000"));
        assertFalse(rule.matches(ctx));
    }
}