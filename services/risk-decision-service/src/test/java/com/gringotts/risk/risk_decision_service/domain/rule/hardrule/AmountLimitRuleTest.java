package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;


import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskPolicyProperties;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
  class AmountLimitRuleTest  {

        private AmountLimitRule  amountLimitRule(BigDecimal maxAmount){
            RiskPolicyProperties prop = new RiskPolicyProperties();
            prop.setMaxSingleAmount(maxAmount);
            return  new AmountLimitRule(prop) ;
        }

        private RiskDecisionContext contextAmount(BigDecimal maxAmount){
            RiskDecisionRequest request = new RiskDecisionRequest();
            request.setAmount(maxAmount);
            return RiskDecisionContext.from(request);
        }
    @Test
    void should_not_match_when_amount_is_below_limit() {
        AmountLimitRule rule = amountLimitRule(new BigDecimal("100000"));

        RiskDecisionContext ctx =
                contextAmount(new BigDecimal("9999"));

        assertFalse(rule.matches(ctx));
    }

    @Test
    void should_not_match_when_amount_equals_limit() {
        AmountLimitRule rule = amountLimitRule(new BigDecimal("10000"));

        RiskDecisionContext ctx =
                contextAmount(new BigDecimal("10000"));

        assertFalse(rule.matches(ctx));
    }

    @Test
    void should_match_when_amount_exceeds_limit() {
        AmountLimitRule rule = amountLimitRule(new BigDecimal("10000"));

        RiskDecisionContext ctx =
                contextAmount(new BigDecimal("10001"));

        assertTrue(rule.matches(ctx));
    }

}
