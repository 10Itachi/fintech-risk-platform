package com.gringotts.risk.risk_decision_service.domain.rule.softrule;

import com.gringotts.enums.TransactionType;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalRiskRule implements SoftRule {

    @Override
    public int score(RiskDecisionContext ctx) {
        return ctx.getRequest().getTransactionType() == TransactionType.WITHDRAWAL
                ? 15
                : 0;
    }

    @Override
    public String code() {
        return "WITHDRAWAL_RISK";
    }
}

