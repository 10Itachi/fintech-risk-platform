package com.gringotts.transaction.transaction_service.domain.model;

import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
//@Index(name = "idx_txn_user_created", columnList = "user_id, created_at")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", nullable = false, length = 36)
    @JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID transactionId;

    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable = false)
    private String sourceAccount;

    @Column(nullable = true)
    private String targetAccount;

    @Column(length = 2, nullable = false)
    private String country;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    //business calculated

    // ML & Audit Snapshots
    @Column(name = "total_amount_24h_snapshot")
    private BigDecimal totalAmountLast24h;

    @Column(name = "txn_count_24h_snapshot")
    private Integer txnCountLast24h;

}
