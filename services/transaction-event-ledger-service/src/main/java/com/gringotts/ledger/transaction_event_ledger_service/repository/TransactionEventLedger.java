package com.gringotts.ledger.transaction_event_ledger_service.repository;

import com.gringotts.ledger.transaction_event_ledger_service.domain.model.TransactionEventLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionEventLedger  extends JpaRepository<TransactionEventLedgerEntity, Long> {
}
