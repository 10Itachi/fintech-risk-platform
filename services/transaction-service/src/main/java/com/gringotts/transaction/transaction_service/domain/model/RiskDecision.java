package com.gringotts.transaction.transaction_service.domain.model;

import com.gringotts.enums.TransactionStatus;


import jakarta.persistence.*;
import lombok.*;



import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "risk_decision")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus decision;

    private Integer riskScore;
    private Double fraudProbability;

    @ElementCollection
    @CollectionTable(name = "risk_reason_codes",
            joinColumns = @JoinColumn(name = "risk_decision_id"))
    @Column(name = "reason_code")
    private List<String> reasonCodes;

    private String modelName;
    private String modelVersion;
    private String policyVersion;

    @Column(nullable = false)
    private Instant evaluatedAt;

    private Long latencyMs;
}
