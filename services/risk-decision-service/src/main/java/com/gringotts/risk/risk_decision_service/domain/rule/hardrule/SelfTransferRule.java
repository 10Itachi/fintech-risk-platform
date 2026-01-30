package com.gringotts.risk.risk_decision_service.domain.rule.hardrule;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.enums.TransactionType;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class SelfTransferRule implements HardRule{
    @Override
    public boolean matches(RiskDecisionContext ctx) {

        RiskDecisionRequest r = ctx.getRequest();

        if (r.getTransactionType()!=TransactionType.TRANSFER) {
            return false;
        }

        return r.getSourceAccount()
                .equals(r.getTargetAccount());
    }

    @Override
    public String reasonCode() {
        return "SELF_TRANSFER_NOT_ALLOWED";
    }
}
