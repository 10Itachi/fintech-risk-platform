package com.gringotts.risk.risk_decision_service.domain.model;

import com.gringotts.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "risk_decision_trace",
        indexes = {
                @Index(name = "idx_evaluated_at", columnList = "evaluated_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_transaction_id", columnNames = "transaction_id")
        }
)
public class RiskDecisionTraceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_status", nullable = false)
    private TransactionStatus finalStatus;

    @Column(name = "ml_probability")
    private Double mlProbability;

    /**
     * Normalized storage for audit clarity.
     * Can be optimized to JSON for ultra high throughput systems.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "risk_decision_reason",
            joinColumns = @JoinColumn(name = "trace_id")
    )
    @Column(name = "reason_code")
    private List<String> reasonCodes;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "trained_at")
    private String trainedAt;

    @Column(name = "policy_version")
    private String policyVersion;
}