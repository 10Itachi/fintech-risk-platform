package com.gringotts.dto;

import com.gringotts.enums.TransactionStatus;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RiskDecisionResponse {

    private TransactionStatus transactionStatus;

    private Integer riskScore;

    private List<String> reasonCodes;

    private Double fraudProbability;

    private String policyVersion;

    private String modelName;

    private String modelVersion;

    private String trainedAt;

    public static RiskDecisionResponse reviewFallback() {
        return RiskDecisionResponse.builder()
                .transactionStatus(TransactionStatus.REVIEW)
                .riskScore(null)
                .reasonCodes(List.of("RISK_SERVICE_UNAVAILABLE"))
                .fraudProbability(null)
                .policyVersion("NOT_AVAILABLE")
                .modelName("NOT_AVAILABLE")
                .modelVersion("NOT_AVAILABLE")
                .trainedAt("NOT_AVAILABLE")
                .build();
    }
}