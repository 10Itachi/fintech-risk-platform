package com.gringotts.ledger.transaction_event_ledger_service.repository;

import com.gringotts.ledger.transaction_event_ledger_service.domain.model.FailedTransactionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FailedTransactionEventRepository extends JpaRepository<FailedTransactionEvent, UUID> {
}
