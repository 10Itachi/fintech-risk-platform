package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TransactionRiskFeatureService {
    private final TransactionRepository transactionRepository;

    private final MeterRegistry meterRegistry;

    public TransactionRiskFeatureService(
            TransactionRepository transactionRepository,
            MeterRegistry meterRegistry
    ) {

        this.transactionRepository = transactionRepository;
        this.meterRegistry = meterRegistry;
    }

    public BigDecimal totalAmountLast24H(String emailId, Instant referenceTime) {
        validateInputs(emailId, referenceTime);
        Timer.Sample timer = Timer.start(meterRegistry);
        Instant windowStart = calculate24HourWindow(referenceTime);
        BigDecimal sum = transactionRepository.sumAmountLast24H(emailId, windowStart);
        timer.stop(
                meterRegistry.timer(
                        "risk.feature.total.amount.latency"
                )
        );

        return sum != null
                ? sum
                : BigDecimal.ZERO;
    }
    public Integer numberOfTransactionsLast24h(String emailId, Instant referenceTime) {
        validateInputs(emailId, referenceTime);
        Timer.Sample timer = Timer.start(meterRegistry);
        Instant windowStart = calculate24HourWindow(referenceTime);
        Integer count= transactionRepository.countTxnsLast24h(emailId, windowStart);
        timer.stop(
                meterRegistry.timer(
                        "risk.feature.txn.count.latency"
                )
        );
        return count != null
                ? count
                : 0;

    }



    private Instant calculate24HourWindow(
            Instant referenceTime
    ) {

        return referenceTime.minus(
                24,
                ChronoUnit.HOURS
        );
    }

    private void validateInputs(
            String emailId,
            Instant referenceTime
    ) {

        if (emailId == null) {

            throw new IllegalArgumentException(
                    "UserId cannot be null "
            );
        }

        if (referenceTime == null) {

            throw new IllegalArgumentException(
                    "Reference time cannot be null"
            );
        }
    }
}
