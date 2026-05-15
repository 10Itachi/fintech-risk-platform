package com.gringotts.transactionobservability.repository;

import com.gringotts.transactionobservability.domain.model.FailedTransactionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedTransactionEvent, UUID> {
    Page<FailedTransactionEvent> findByFailedAtBetween(
            Instant from,
            Instant to,
            Pageable pageable
    );
}
