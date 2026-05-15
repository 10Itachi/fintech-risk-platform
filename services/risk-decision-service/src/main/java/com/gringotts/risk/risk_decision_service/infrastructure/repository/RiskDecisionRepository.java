package com.gringotts.risk.risk_decision_service.infrastructure.repository;

import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RiskDecisionRepository extends JpaRepository<RiskDecisionTraceEntity, Long> {

    /**
     * Idempotency check (1:1 transaction → decision)
     * MUST be backed by UNIQUE index in DB
     */
    Optional<RiskDecisionTraceEntity> findByTransactionId(String transactionId);

    /**
     * Time-range based pagination (audit / admin dashboards)
     * Efficient only if evaluatedAt is indexed
     */
    Page<RiskDecisionTraceEntity> findByEvaluatedAtBetween(
            Instant from,
            Instant to,
            Pageable pageable
    );
}
