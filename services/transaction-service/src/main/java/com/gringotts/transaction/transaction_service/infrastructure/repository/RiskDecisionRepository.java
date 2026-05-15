package com.gringotts.transaction.transaction_service.infrastructure.repository;

import com.gringotts.transaction.transaction_service.domain.model.RiskDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RiskDecisionRepository extends JpaRepository<RiskDecision, UUID> {
    Optional<Object> findByTransactionId(UUID transactionId);
}
