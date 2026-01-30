package com.gringotts.risk.risk_decision_service.domain.decision;


import com.gringotts.enums.TransactionStatus;
import org.springframework.stereotype.Component;

@Component
public class RiskDecisionPolicy {

    private final RiskPolicyProperties props;

    public RiskDecisionPolicy(RiskPolicyProperties props) {
        this.props = props;
    }

    public TransactionStatus decide (RiskDecisionContext context) {
        // for hard rule
        if (context.isDeclined()) return TransactionStatus.DECLINED;

        // 2. High Risk Threshold (ML or Soft Score)
        if (context.getMlProbability() >= props.getDeclineThreshold() ||
                context.getSoftScore() >= props.getSoftDeclineScore()) {
            context.addReason("POLICY_DECLINE_LIMIT_EXCEEDED");
            return TransactionStatus.DECLINED;
        }

        // 3. Medium Risk Threshold (Requires Manual Review)
        if (context.getMlProbability() >= props.getReviewThreshold() ||
                context.getSoftScore() >= props.getSoftReviewScore()) {
            context.addReason("POLICY_REVIEW_REQUIRED");
            return TransactionStatus.REVIEW;
        }
        // 4. Otherwise Approve
        return TransactionStatus.APPROVED;
    }

}
