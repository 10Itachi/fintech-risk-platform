package com.gringotts.dto;
import com.gringotts.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RiskDecisionResponse {

    private TransactionStatus transactionStatus;
    private Integer riskScore;
    private List<String> reasonCode;
    private Double fraudProbability;
    private String policyVersion;
    private String modelName;
    private String modelVersion;
    private String trainedAt;
    public static RiskDecisionResponse reviewFallback() {

        return RiskDecisionResponse.builder()
                .transactionStatus(TransactionStatus.REVIEW)
                .riskScore(null)
                .reasonCode(List.of("RISK_SERVICE_UNAVAILABLE"))
                .fraudProbability(null)
                .policyVersion("RISK_POLICY_VERSION_NOT_AVAILABLE")
                .modelName("MODEL_UNAVAILABLE")
                .modelVersion("MODEL_UNAVAILABLE")
                .trainedAt("MODEL_UNAVAILABLE")
                .build();
    }

}
