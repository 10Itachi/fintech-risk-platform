package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HardRuleEngineTest {

    @Mock
    HardRule rule1;

    @Mock
    HardRule rule2;

    @Test
    void should_return_reason_of_first_matching_rule() {
        RiskDecisionContext ctx = mock(RiskDecisionContext.class);

        when(rule1.matches(ctx)).thenReturn(true);
        when(rule1.reasonCode()).thenReturn("RULE_1");

        HardRuleEngine engine =
                new HardRuleEngine(List.of(rule1, rule2));

        List<String> result = engine.evaluate(ctx);

        assertTrue(result.contains("RULE_1"));
        assertEquals("RULE_1", result.get(0));
    }

    @Test
    void should_return_reason_of_second_rule_when_first_does_not_match() {
        RiskDecisionContext ctx = mock(RiskDecisionContext.class);

        when(rule1.matches(ctx)).thenReturn(false);
        when(rule2.matches(ctx)).thenReturn(true);
        when(rule2.reasonCode()).thenReturn("RULE_2");

        HardRuleEngine engine =
                new HardRuleEngine(List.of(rule1, rule2));

        List<String> result = engine.evaluate(ctx);

        assertEquals("RULE_2", result.get(0));
    }

    @Test
    void should_return_empty_when_no_rule_matches() {
        RiskDecisionContext ctx = mock(RiskDecisionContext.class);

        when(rule1.matches(ctx)).thenReturn(false);
        when(rule2.matches(ctx)).thenReturn(false);

        HardRuleEngine engine =
                new HardRuleEngine(List.of(rule1, rule2));

        List<String> result = engine.evaluate(ctx);

        assertTrue(result.isEmpty());
    }
}
