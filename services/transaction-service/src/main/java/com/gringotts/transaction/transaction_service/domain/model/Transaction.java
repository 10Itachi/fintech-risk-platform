package com.gringotts.transaction.transaction_service.domain.model;

import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.enums.TransactionType;
import com.gringotts.transaction.transaction_service.domain.businessEnums.InternalTransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.apache.kafka.common.protocol.types.Field;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
//@EntityListeners(AuditingEntityListener.class) // enable later when auditing is configured
//@Index(name = "idx_txn_user_created", columnList = "user_id, created_at")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    // =========================
    // Identity
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", nullable = false, length = 36)
    @JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID transactionId;

    @Column(nullable = false, updatable = false)
    private UUID userId;


    @Column(nullable = false, updatable = false)
    private String userName;

    @Column(nullable = false,updatable = false)
    @Email
    private String email;
    // =========================
    // Business Data
    // =========================

    @Column(nullable = false)
    private BigDecimal amount;

    // External status (client-facing, from contracts)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    // Internal status (orchestration lifecycle)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InternalTransactionStatus internalStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Channel channel;

    // Must be NOT NULL (business critical field)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    // =========================
    // Account Info
    // =========================

    @Column(nullable = true)
    private String targetAccount;

    // Increased flexibility (ISO + custom codes)
    @Column(length = 3, nullable = false)
    private String country;

    @Column(nullable = false)
    private String deviceId;

    // =========================
    // Time Tracking
    // =========================

    // Actual business event time (from request)
    @Column(nullable = false, updatable = false)
    private Instant transactionTime;

    // DB creation time
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Updated on every state change (risk, ledger, etc.)
    @Column(nullable = false)
    private Instant updatedAt;

    // =========================
    // Idempotency (Critical for fintech)
    // =========================

    @Column(unique = true, nullable = true)
    private String idempotencyKey;

    // =========================
    // Risk Feature Snapshot (input to risk engine)
    // =========================

    @Column(name = "total_amount_24h_snapshot")
    private BigDecimal totalAmountLast24h;

    @Column(name = "txn_count_24h_snapshot")
    private Integer txnCountLast24h;

    @Column(nullable = false)
    private String sourceAccount;

    /*
    Internal → transactional Status Mapping

    | Internal             | transactional     |
    |---------------------|--------------|
    | INITIATED           | INITIATED    |
    | PENDING_RISK        | INITIATED    |
    | RISK_APPROVED       | INITIATED    |
    | LEDGER_PENDING      | INITIATED    |
    | LEDGER_SUCCESS      | APPROVED     |
    | COMPLETED           | APPROVED     |
    | RISK_REJECTED       | DECLINED     |
    | LEDGER_FAILED       | DECLINED     |
    | FAILED              | DECLINED     |
    */
}