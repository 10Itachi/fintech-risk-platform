package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.transaction.transaction_service.config.security.SecurityUtils;
import com.gringotts.transaction.transaction_service.domain.exception.InvalidUserContextException;
import com.gringotts.transaction.transaction_service.domain.exception.RiskDecisionNotFoundException;
import com.gringotts.transaction.transaction_service.domain.exception.TransactionNotFoundException;
import com.gringotts.transaction.transaction_service.domain.model.RiskDecision;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.infrastructure.repository.RiskDecisionRepository;
import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import com.gringotts.transaction.transaction_service.mapper.TransactionMapper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class TransactionQueryService {

    /*
     =========================================================
     PAGINATION / SORT CONFIG
     =========================================================
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "amount",
            "transactionStatus",
            "internalStatus"
    );

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final RiskDecisionRepository riskDecisionRepository;

    private final MeterRegistry meterRegistry;

    public TransactionQueryService(
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            RiskDecisionRepository riskDecisionRepository,
            MeterRegistry meterRegistry
    ) {

        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.riskDecisionRepository = riskDecisionRepository;
        this.meterRegistry = meterRegistry;
    }

    /*
     =========================================================
     USER → GET TRANSACTION BY ID
     =========================================================

     Security critical:
     User can ONLY access own transactions.
     */
    public TransactionResponseDto getById(
            UUID transactionId
    ) {

        Timer.Sample timer =
                Timer.start(meterRegistry);

        if (transactionId == null) {

            throw new IllegalArgumentException(
                    "TransactionId cannot be null"
            );
        }

        UUID userId =
                SecurityUtils.getUserId();

        if (userId == null ) {

            throw new IllegalArgumentException(
                    "User Id cannot be null"
            );
        }

        /*
         =====================================================
         ENFORCE OWNERSHIP
         =====================================================
         */
        Transaction txn =
                (Transaction) transactionRepository
                        .findByTransactionIdAndUserId(
                                transactionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction not found for id: "
                                                + transactionId
                                )
                        );

        log.info(
                "transaction_fetched txnId={} userId={} status={}",
                txn.getTransactionId(),
                userId,
                txn.getTransactionStatus()
        );

        meterRegistry.counter(
                "transaction.query.by.id"
        ).increment();

        timer.stop(
                meterRegistry.timer(
                        "transaction.query.by.id.latency"
                )
        );

        return transactionMapper.toDto(txn);
    }

    /*
     =========================================================
     USER → GET MY TRANSACTIONS
     =========================================================
     */
    public Page<TransactionResponseDto> getMyTransactions(
            int page,
            int size,
            String sort
    ) {

        Timer.Sample timer =
                Timer.start(meterRegistry);

        UUID userId =
                SecurityUtils.getUserId();

        if (userId == null ) {

            throw new IllegalArgumentException(
                    "User Id cannot be null"
            );
        }

        Pageable pageable =
                buildPageable(page, size, sort);

        Page<Transaction> txnPage =
                transactionRepository.findByUserId(
                        userId,
                        pageable
                );

        log.info(
                "user_transactions_fetched userId={} count={}",
                userId,
                txnPage.getNumberOfElements()
        );

        meterRegistry.counter(
                "transaction.query.user.transactions"
        ).increment();

        timer.stop(
                meterRegistry.timer(
                        "transaction.query.user.transactions.latency"
                )
        );

        return txnPage.map(transactionMapper::toDto);
    }

    /*
     =========================================================
     ADMIN → GET USER TRANSACTIONS
     =========================================================
     */
    public Page<TransactionResponseDto> getAllTransactionsByUserId(
            UUID userId,
            int page,
            int size,
            String sort
    ) {

        Timer.Sample timer =
                Timer.start(meterRegistry);

        if (userId == null ) {

            throw new IllegalArgumentException(
                    "UserId cannot be null "
            );
        }

        Pageable pageable =
                buildPageable(page, size, sort);

        Page<Transaction> txnPage =
                transactionRepository.findByUserId(
                        userId,
                        pageable
                );

        log.info(
                "admin_user_transactions_fetched userId={} count={}",
                userId,
                txnPage.getNumberOfElements()
        );

        meterRegistry.counter(
                "transaction.query.admin.user.transactions"
        ).increment();

        timer.stop(
                meterRegistry.timer(
                        "transaction.query.admin.user.transactions.latency"
                )
        );

        return txnPage.map(transactionMapper::toDto);
    }

    /*
     =========================================================
     ADMIN → GET ALL TRANSACTIONS
     =========================================================
     */
    public Page<TransactionResponseDto> getAllTransactions(
            int page,
            int size,
            String sort
    ) {

        Timer.Sample timer =
                Timer.start(meterRegistry);

        Pageable pageable =
                buildPageable(page, size, sort);

        Page<Transaction> txnPage =
                transactionRepository.findAll(pageable);

        log.info(
                "admin_transactions_fetched count={}",
                txnPage.getNumberOfElements()
        );

        meterRegistry.counter(
                "transaction.query.admin.all"
        ).increment();

        timer.stop(
                meterRegistry.timer(
                        "transaction.query.admin.all.latency"
                )
        );

        return txnPage.map(transactionMapper::toDto);
    }

    /*
     =========================================================
     ADMIN → GET RISK DECISION
     =========================================================
     */
    public RiskDecision getRiskDecisionByTransId(
            UUID transactionId
    ) {

        Timer.Sample timer =
                Timer.start(meterRegistry);

        if (transactionId == null) {

            throw new IllegalArgumentException(
                    "TransactionId cannot be null"
            );
        }

        RiskDecision decision =
                (RiskDecision) riskDecisionRepository
                        .findByTransactionId(transactionId)
                        .orElseThrow(() ->
                                new RiskDecisionNotFoundException(
                                        "RiskDecision not found for txnId: "
                                                + transactionId
                                )
                        );

        log.info(
                "risk_decision_fetched txnId={} decision={} score={}",
                transactionId,
                decision.getDecision(),
                decision.getRiskScore()
        );

        meterRegistry.counter(
                "transaction.query.risk.decision"
        ).increment();

        timer.stop(
                meterRegistry.timer(
                        "transaction.query.risk.decision.latency"
                )
        );

        return decision;
    }

    /*
     =========================================================
     INTERNAL → PAGEABLE BUILDER
     =========================================================
     */
    private Pageable buildPageable(
            int page,
            int size,
            String sort
    ) {

        /*
         =====================================================
         PAGE VALIDATION
         =====================================================
         */
        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        }

        /*
         Prevent abusive page sizes.
         */
        size = Math.min(size, MAX_PAGE_SIZE);

        /*
         =====================================================
         DEFAULT SORT
         =====================================================
         */
        if (sort == null || sort.isBlank()) {

            return PageRequest.of(
                    page,
                    size,
                    Sort.by(
                            Sort.Direction.DESC,
                            DEFAULT_SORT_FIELD
                    )
            );
        }

        try {

            String[] sortParts =
                    sort.split(",");

            String field =
                    sortParts[0];

            /*
             =================================================
             SORT FIELD WHITELIST
             =================================================
             */
            if (!ALLOWED_SORT_FIELDS.contains(field)) {

                log.warn(
                        "invalid_sort_field field={} fallback={}",
                        field,
                        DEFAULT_SORT_FIELD
                );

                field = DEFAULT_SORT_FIELD;
            }

            Sort.Direction direction =
                    sortParts.length > 1
                            && sortParts[1]
                            .equalsIgnoreCase("asc")
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;

            return PageRequest.of(
                    page,
                    size,
                    Sort.by(direction, field)
            );

        } catch (Exception ex) {

            log.warn(
                    "invalid_sort_parameter sort={} fallback_default=true",
                    sort
            );

            return PageRequest.of(
                    page,
                    size,
                    Sort.by(
                            Sort.Direction.DESC,
                            DEFAULT_SORT_FIELD
                    )
            );
        }
    }
}