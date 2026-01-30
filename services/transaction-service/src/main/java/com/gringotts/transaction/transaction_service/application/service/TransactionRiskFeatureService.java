package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TransactionRiskFeatureService {
    private final TransactionRepository transactionRepository;
    public TransactionRiskFeatureService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public BigDecimal totalAmountLast24H(Long userId, Instant now) {
        Instant windowStart = now.minus(24, ChronoUnit.HOURS);
        BigDecimal sum = transactionRepository.sumAmountLast24H(userId, windowStart);

        return sum != null ? sum : BigDecimal.ZERO;
    }
    public Integer numberOfTransactionsLast24h(Long userId, Instant now) {
        Instant windowStart = now.minus(24, ChronoUnit.HOURS);
        Integer count= transactionRepository.countTxnsLast24h(userId, windowStart);
        return count != null ? count : 0;
    }
}
