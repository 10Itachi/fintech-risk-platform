package com.gringotts.risk.risk_decision_service.domain.decision;

import com.gringotts.enums.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.gringotts.enums.TransactionStatus.*;

@Component
public class RiskDecisionPolicy {

    private static final Logger log = LoggerFactory.getLogger(RiskDecisionPolicy.class);
    private final RiskPolicyProperties props;

    public RiskDecisionPolicy(RiskPolicyProperties props) {
        this.props = props;
    }

    public TransactionStatus decide(RiskDecisionContext context) {

        // 1. If it's already hard-failed, we don't need ML stats
        if (context.isDeclined()) {
            return TransactionStatus.DECLINED;
        }

        // 2. Otherwise, we proceed with ML-based logic
        context.ensureMlEvaluated();
        double finalRisk = context.getAdjustedRisk();
        double baseProb = context.getMlProbability();

        TransactionStatus decision;

        // HIGH RISK → DECLINE
        if (finalRisk >= props.getDeclineThreshold()) {
            context.addReason("RISK_HIGH");
            decision = DECLINED;
        }

        // MEDIUM RISK → REVIEW
        else if (finalRisk >= props.getReviewThreshold()) {
            context.addReason("RISK_MEDIUM");
            decision = REVIEW;
        }

        // LOW RISK → APPROVE
        else {
            decision = APPROVED;
        }


        // LOG (AUDIT-GRADE)
        log.info(
                "Decision | txnId={} | decision={} | finalRisk={} | mlProb={} | reasons={} | modelVersion={} | policyVersion={} | policyActivatedAt={}",
                context.getRequest().getTransactionId(),
                decision,
                finalRisk,
                baseProb,
                context.getReasonCodes(),
                context.getModelMetadataSafe().getModelVersion(),
                context.getPolicyVersionSafe(),
                context.getPolicyActivatedAtSafe()
        );
        return decision;
    }
}