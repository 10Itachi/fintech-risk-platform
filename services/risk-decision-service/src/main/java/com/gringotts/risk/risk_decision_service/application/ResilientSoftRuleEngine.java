package com.gringotts.risk.risk_decision_service.application;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.rule.softrule.SoftRuleEngine;
import com.gringotts.risk.risk_decision_service.enums.RiskFailureType;
import com.gringotts.risk.risk_decision_service.exception.RiskEvaluationException;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class ResilientSoftRuleEngine {

    private final SoftRuleEngine softRuleEngine;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final long TIMEOUT_MS = 200;

    public ResilientSoftRuleEngine(SoftRuleEngine softRuleEngine) {
        this.softRuleEngine = softRuleEngine;
    }

    public void evaluate(RiskDecisionContext ctx) {

        Future<?> future = executor.submit(() -> softRuleEngine.evaluate(ctx));

        try {
            future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new RiskEvaluationException(RiskFailureType.SOFT_RULE_TIMEOUT, ex);
        } catch (ExecutionException ex) {
            throw new RiskEvaluationException(RiskFailureType.SOFT_RULE_FAILURE, ex.getCause());
        } catch (Exception ex) {
            throw new RiskEvaluationException(RiskFailureType.SOFT_RULE_FAILURE, ex);
        }
    }
}
