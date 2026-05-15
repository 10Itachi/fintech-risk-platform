package com.gringotts.transaction.transaction_service.infrastructure.repository;

import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

        Page<Transaction> findByUserId(UUID userId, Pageable pageable);

        @Query("""
                SELECT COALESCE(SUM(t.amount), 0)
                FROM Transaction t
                WHERE t.userId =:userId
                AND t.createdAt >= :windowStart
                AND t.transactionStatus IN ('APPROVED','INITIATED')
                """)
        BigDecimal sumAmountLast24H(@Param ("userId")  UUID userId,
                                    @Param("windowStart")Instant windowStart);

        @Query("""
                SELECT COUNT(t)
                FROM Transaction t
                WHERE t.userId = :userId
                AND t.createdAt >= :windowStart
                AND t.transactionStatus IN ('APPROVED', 'INITIATED')
                """)
        Integer countTxnsLast24h(@Param ("userId")  UUID userId,
                             @Param("windowStart")Instant windowStart);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Object> findByTransactionIdAndUserId(UUID transactionId, UUID userId);
}
