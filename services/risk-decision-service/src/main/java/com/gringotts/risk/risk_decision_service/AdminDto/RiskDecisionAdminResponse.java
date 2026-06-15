    package com.gringotts.risk.risk_decision_service.AdminDto;

    import com.gringotts.enums.TransactionStatus;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import java.time.Instant;
    import java.util.List;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class RiskDecisionAdminResponse {

        private Long id;

        private String transactionId;

        private TransactionStatus finalStatus;

        private Integer softRiskScore;

        private Double mlProbability;

        private List<String> reasonCodes;

        private Instant evaluatedAt;

        private String modelName;

        private String modelVersion;

        private String trainedAt;
    }
