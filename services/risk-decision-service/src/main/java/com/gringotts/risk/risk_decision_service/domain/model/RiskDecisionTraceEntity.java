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
@Table(name = "risk_decision_traces")
public class RiskDecisionTraceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus finalStatus;

    private Integer softRiskScore;

    private Double mlProbability;

    @ElementCollection
    @CollectionTable(
            name = "risk_decision_reason",
            joinColumns = @JoinColumn(name = "trace_id")
    )
    @Column(name = "reason_code")
    private List<String> reasonCodes;

    private Instant evaluatedAt;

    @Column
    private String modelName;
    @Column
    private String modelVersion;
    @Column
    private String trainedAt;

    //public RiskDecisionTraceEntity(String string, TransactionStatus finalStatus, int softRiskScore, double mlProbability, List<String> reasonCodes, Instant evaluatedAt) {
    //}
}
