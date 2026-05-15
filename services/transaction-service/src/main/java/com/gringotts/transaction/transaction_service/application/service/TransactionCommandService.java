package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.transaction.transaction_service.domain.businessEnums.InternalTransactionStatus;
import com.gringotts.transaction.transaction_service.domain.exception.InvalidTransactionStateException;
import com.gringotts.transaction.transaction_service.domain.model.OutboxEvent;
import com.gringotts.transaction.transaction_service.domain.model.RiskDecision;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.domain.policy.TransactionLifecyclePolicy;
import com.gringotts.transaction.transaction_service.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.infrastructure.repository.OutboxEventRepository;
import com.gringotts.transaction.transaction_service.infrastructure.repository.RiskDecisionRepository;
import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import com.gringotts.transaction.transaction_service.mapper.RiskDecisionMapper;
import com.gringotts.transaction.transaction_service.mapper.TransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static com.gringotts.enums.TransactionStatus.DECLINED;

@Service
@Slf4j
public class TransactionCommandService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final RiskDecisionRepository riskDecisionRepository;
    private final RiskDecisionMapper riskDecisionMapper;
    private final OutboxService outboxService;
    private final OutboxEventRepository outboxEventRepository;

    public TransactionCommandService(TransactionRepository transactionRepository,
                                     TransactionMapper transactionMapper,
                                     RiskDecisionRepository riskDecisionRepository,
                                     RiskDecisionMapper riskDecisionMapper, OutboxService outboxService, OutboxEventRepository outboxEventRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.riskDecisionRepository = riskDecisionRepository;
        this.riskDecisionMapper = riskDecisionMapper;
        this.outboxService = outboxService;
        this.outboxEventRepository = outboxEventRepository;
    }

    // TX1 → CREATE
    @Transactional
    public Transaction createInitialTransaction(Transaction txn) {

        TransactionLifecyclePolicy.validateTransition(
                txn.getInternalStatus(),
                InternalTransactionStatus.PENDING_RISK
        );

        txn.setInternalStatus(InternalTransactionStatus.PENDING_RISK);
        txn.setTotalAmountLast24h(txn.getTotalAmountLast24h());
        txn.setTxnCountLast24h(txn.getTxnCountLast24h());
        txn.setCreatedAt(Instant.now());
        txn.setUpdatedAt(Instant.now());

        return transactionRepository.save(txn);
    }

    // =========================
    // TX2 → FINALIZE
    // =========================
    @Transactional
    public TransactionResponseDto finalizeTransaction(Transaction txn,
                                                      RiskDecisionResponse response,
                                                      long latency) {

        if (txn == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        if (response == null) {
            throw new IllegalArgumentException("Risk response cannot be null");
        }

        // MAP DECISION
        RiskDecision decision =
                riskDecisionMapper.toEntity(txn, response, latency);
        riskDecisionRepository.save(decision);

        // DETERMINE NEXT STATE
        InternalTransactionStatus nextState = switch (response.getTransactionStatus()) {

            case APPROVED -> InternalTransactionStatus.RISK_APPROVED;

            case DECLINED -> InternalTransactionStatus.RISK_REJECTED;

            case REVIEW -> InternalTransactionStatus.REVIEW_PENDING;

            default -> throw new InvalidTransactionStateException(
                    "Unsupported risk status: " + response.getTransactionStatus()
            );
        };

        // =========================
        // VALIDATE TRANSITION (CRITICAL)
        // =========================
        TransactionLifecyclePolicy.validateTransition(
                txn.getInternalStatus(),
                nextState
        );

        txn.setInternalStatus(nextState);
        txn.setTransactionStatus(response.getTransactionStatus());
        txn.setUpdatedAt(Instant.now());

        txn = transactionRepository.save(txn);

        log.info("Transaction finalized | txnId={} | status={}",
                txn.getTransactionId(), txn.getTransactionStatus());

        // CREATE OUTBOX EVENT
        OutboxEvent outboxEvent = outboxService.buildTransactionFinalizedEvent(txn);
        outboxEventRepository.save(outboxEvent);

        return transactionMapper.toDto(txn);
    }

    @Transactional
    public TransactionResponseDto processManualReview(UUID transactionId,
                                                      TransactionStatus adminDecision) {

        if (transactionId == null) {
            throw new IllegalArgumentException("TransactionId cannot be null");
        }

        if (adminDecision == null) {
            throw new IllegalArgumentException("Admin decision cannot be null");
        }

        // FETCH TRANSACTION
        Transaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Transaction not found: " + transactionId));


        // STATE VALIDATION (MUST BE REVIEW_PENDING)
        if (txn.getInternalStatus() != InternalTransactionStatus.REVIEW_PENDING) {
            throw new InvalidTransactionStateException(
                    "Manual review allowed only for REVIEW_PENDING transactions. Current state: "
                            + txn.getInternalStatus()
            );
        }

        // DETERMINE NEXT STATE
        InternalTransactionStatus nextState = switch (adminDecision) {
            case APPROVED -> InternalTransactionStatus.RISK_APPROVED;
            case DECLINED -> InternalTransactionStatus.RISK_REJECTED;
            default -> throw new InvalidTransactionStateException(
                    "Invalid admin decision: " + adminDecision +
                            ". Only APPROVED or DECLINED allowed."
            );
        };

        // VALIDATE LIFECYCLE TRANSITION
        TransactionLifecyclePolicy.validateTransition(
                txn.getInternalStatus(),
                nextState
        );

        // APPLY STATE CHANGE
        txn.setInternalStatus(nextState);
        txn.setTransactionStatus(adminDecision);
        txn.setUpdatedAt(Instant.now());

        txn = transactionRepository.save(txn);

        log.info("Manual review processed | txnId={} | decision={} | newState={}",
                txn.getTransactionId(),
                adminDecision,
                nextState);
        OutboxEvent outboxEvent = outboxService.buildTransactionFinalizedEvent(txn);
        outboxEventRepository.save(outboxEvent);
        return transactionMapper.toDto(txn);
    }
}