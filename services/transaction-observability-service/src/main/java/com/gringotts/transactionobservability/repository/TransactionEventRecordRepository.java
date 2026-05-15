package com.gringotts.transactionobservability.repository;


import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionEventRecordRepository extends JpaRepository<TransactionEventRecord, Long> {

    Optional<TransactionEventRecord> findByTransactionId(UUID transactionId);

    boolean existsByTransactionId(UUID transactionId);

    Page<TransactionEventRecord> findByRecordedAtBetween(
            Instant from,
            Instant to,
            Pageable pageable
    );

    List<TransactionEventRecord> findByRecordedAtBetween(Instant from, Instant to);
}