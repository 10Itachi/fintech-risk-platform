package com.gringotts.transactionobservability.service;

import com.gringotts.transactionobservability.domain.model.FailedTransactionEvent;
import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;
import com.gringotts.transactionobservability.exception.FailedEventNotFoundException;
import com.gringotts.transactionobservability.exception.TransactionNotFoundException;
import com.gringotts.transactionobservability.repository.FailedEventRepository;
import com.gringotts.transactionobservability.repository.TransactionEventRecordRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TransactionObservabilityQueryService {

    /*
     =========================================================
     EXPORT SAFETY LIMIT
     =========================================================
     */
    private static final int MAX_EXPORT_RECORDS = 10_000;

    private final TransactionEventRecordRepository transactionEventRecordRepository;
    private final FailedEventRepository failedEventRepository;

    /*
     =========================================================
     METRICS
     =========================================================
     */
    private final Counter transactionQueryCounter;
    private final Counter failedEventQueryCounter;
    private final Counter exportCounter;
    private final Counter exportFailureCounter;

    private final Timer transactionQueryTimer;
    private final Timer failedEventQueryTimer;
    private final Timer exportTimer;

    private final DistributionSummary exportRowsMetric;

    public TransactionObservabilityQueryService(
            TransactionEventRecordRepository transactionEventRecordRepository,
            FailedEventRepository failedEventRepository,
            MeterRegistry meterRegistry
    ) {

        this.transactionEventRecordRepository =
                transactionEventRecordRepository;

        this.failedEventRepository =
                failedEventRepository;

        /*
         =========================================================
         COUNTERS
         =========================================================
         */
        this.transactionQueryCounter =
                meterRegistry.counter(
                        "transaction.observability.query.transactions"
                );

        this.failedEventQueryCounter =
                meterRegistry.counter(
                        "transaction.observability.query.failed.events"
                );

        this.exportCounter =
                meterRegistry.counter(
                        "transaction.observability.export.success"
                );

        this.exportFailureCounter =
                meterRegistry.counter(
                        "transaction.observability.export.failed"
                );

        /*
         =========================================================
         TIMERS
         =========================================================
         */
        this.transactionQueryTimer =
                meterRegistry.timer(
                        "transaction.observability.query.transactions.time"
                );

        this.failedEventQueryTimer =
                meterRegistry.timer(
                        "transaction.observability.query.failed.events.time"
                );

        this.exportTimer =
                meterRegistry.timer(
                        "transaction.observability.export.time"
                );

        /*
         =========================================================
         DISTRIBUTION
         =========================================================
         */
        this.exportRowsMetric =
                meterRegistry.summary(
                        "transaction.observability.export.rows"
                );
    }

    public TransactionEventRecord getByTransactionId(UUID txnId) {

        Timer.Sample timer = Timer.start();

        try {

            TransactionEventRecord record =
                    transactionEventRecordRepository
                            .findByTransactionId(txnId)
                            .orElseThrow(() ->
                                    new TransactionNotFoundException(txnId)
                            );

            transactionQueryCounter.increment();

            log.info(
                    "event=transaction_query_success txnId={}",
                    txnId
            );

            return record;

        } finally {

            timer.stop(transactionQueryTimer);
        }
    }

    public Page<TransactionEventRecord> getAll(
            Instant from,
            Instant to,
            Pageable pageable
    ) {

        Timer.Sample timer = Timer.start();

        try {

            Page<TransactionEventRecord> result;

            if (from != null && to != null) {

                result =
                        transactionEventRecordRepository
                                .findByRecordedAtBetween(
                                        from,
                                        to,
                                        pageable
                                );

            } else {

                result =
                        transactionEventRecordRepository
                                .findAll(pageable);
            }

            transactionQueryCounter.increment();

            log.info(
                    "event=transaction_page_query from={} to={} page={} size={} returned={}",
                    from,
                    to,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    result.getNumberOfElements()
            );

            return result;

        } finally {

            timer.stop(transactionQueryTimer);
        }
    }

    public Page<FailedTransactionEvent> getFailedEvents(
            Instant from,
            Instant to,
            Pageable pageable
    ) {

        Timer.Sample timer = Timer.start();

        try {

            Page<FailedTransactionEvent> result;

            if (from != null && to != null) {

                result =
                        failedEventRepository
                                .findByFailedAtBetween(
                                        from,
                                        to,
                                        pageable
                                );

            } else {

                result =
                        failedEventRepository
                                .findAll(pageable);
            }

            failedEventQueryCounter.increment();

            log.info(
                    "event=failed_event_query from={} to={} page={} size={} returned={}",
                    from,
                    to,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    result.getNumberOfElements()
            );

            return result;

        } finally {

            timer.stop(failedEventQueryTimer);
        }
    }

    public FailedTransactionEvent getFailedEvent(UUID id) {

        Timer.Sample timer = Timer.start();

        try {

            FailedTransactionEvent event =
                    failedEventRepository.findById(id)
                            .orElseThrow(() ->
                                    new FailedEventNotFoundException(id)
                            );

            failedEventQueryCounter.increment();

            log.info(
                    "event=failed_event_lookup_success failedEventId={}",
                    id
            );

            return event;

        } finally {

            timer.stop(failedEventQueryTimer);
        }
    }

    public List<TransactionEventRecord> exportTransactions(
            Instant from,
            Instant to
    ) {

        Timer.Sample timer = Timer.start();

        try {

            /*
             =====================================================
             VALIDATION
             =====================================================
             */
            if (from != null
                    && to != null
                    && from.isAfter(to)) {

                exportFailureCounter.increment();

                throw new IllegalArgumentException(
                        "Invalid export date range"
                );
            }

            List<TransactionEventRecord> result =
                    transactionEventRecordRepository
                            .findByRecordedAtBetween(from, to);

            /*
             =====================================================
             EXPORT PROTECTION
             =====================================================
             */
            if (result.size() > MAX_EXPORT_RECORDS) {

                exportFailureCounter.increment();

                log.warn(
                        "event=export_limit_exceeded from={} to={} size={} limit={}",
                        from,
                        to,
                        result.size(),
                        MAX_EXPORT_RECORDS
                );

                throw new IllegalArgumentException(
                        "Export exceeds maximum allowed records"
                );
            }

            exportCounter.increment();

            exportRowsMetric.record(result.size());

            log.info(
                    "event=transaction_export_generated from={} to={} rows={}",
                    from,
                    to,
                    result.size()
            );

            return result;

        } finally {

            timer.stop(exportTimer);
        }
    }
}