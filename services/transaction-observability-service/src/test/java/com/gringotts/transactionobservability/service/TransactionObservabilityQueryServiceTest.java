package com.gringotts.transactionobservability.service;

import com.gringotts.transactionobservability.domain.model.FailedTransactionEvent;
import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;
import com.gringotts.transactionobservability.exception.FailedEventNotFoundException;
import com.gringotts.transactionobservability.exception.TransactionNotFoundException;
import com.gringotts.transactionobservability.repository.FailedEventRepository;
import com.gringotts.transactionobservability.repository.TransactionEventRecordRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionObservabilityQueryServiceTest {

    @Mock
    private TransactionEventRecordRepository transactionEventRecordRepository;

    @Mock
    private FailedEventRepository failedEventRepository;

    private TransactionObservabilityQueryService service;

    private final SimpleMeterRegistry meterRegistry =
            new SimpleMeterRegistry();

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        service = new TransactionObservabilityQueryService(
                transactionEventRecordRepository,
                failedEventRepository,
                meterRegistry
        );
    }

    /*
     =========================================================
     TRANSACTION LOOKUP SUCCESS
     =========================================================
     */
    @Test
    @DisplayName("Should return transaction record by transactionId")
    void shouldReturnTransactionById() {

        UUID txnId = UUID.randomUUID();

        TransactionEventRecord record =
                TransactionEventRecord.builder()
                        .transactionId(txnId)
                        .build();

        when(transactionEventRecordRepository
                .findByTransactionId(txnId))
                .thenReturn(Optional.of(record));

        TransactionEventRecord result =
                service.getByTransactionId(txnId);

        assertThat(result)
                .isNotNull();

        assertThat(result.getTransactionId())
                .isEqualTo(txnId);

        verify(transactionEventRecordRepository)
                .findByTransactionId(txnId);

        assertThat(
                meterRegistry
                        .counter("transaction.observability.query.transactions")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     TRANSACTION NOT FOUND
     =========================================================
     */
    @Test
    @DisplayName("Should throw when transaction not found")
    void shouldThrowWhenTransactionNotFound() {

        UUID txnId = UUID.randomUUID();

        when(transactionEventRecordRepository
                .findByTransactionId(txnId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getByTransactionId(txnId)
        )
                .isInstanceOf(TransactionNotFoundException.class);
    }

    /*
     =========================================================
     PAGE QUERY WITH DATE RANGE
     =========================================================
     */
    @Test
    @DisplayName("Should fetch paginated transactions with date range")
    void shouldFetchTransactionsWithDateRange() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Instant from =
                Instant.now().minusSeconds(3600);

        Instant to =
                Instant.now();

        Page<TransactionEventRecord> page =
                new PageImpl<>(List.of(
                        TransactionEventRecord.builder().build()
                ));

        when(transactionEventRecordRepository
                .findByRecordedAtBetween(from, to, pageable))
                .thenReturn(page);

        Page<TransactionEventRecord> result =
                service.getAll(from, to, pageable);

        assertThat(result.getContent())
                .hasSize(1);

        verify(transactionEventRecordRepository)
                .findByRecordedAtBetween(from, to, pageable);

        assertThat(
                meterRegistry
                        .counter("transaction.observability.query.transactions")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     PAGE QUERY WITHOUT DATE RANGE
     =========================================================
     */
    @Test
    @DisplayName("Should fetch all paginated transactions")
    void shouldFetchAllTransactions() {

        Pageable pageable =
                PageRequest.of(0, 20);

        Page<TransactionEventRecord> page =
                new PageImpl<>(List.of(
                        TransactionEventRecord.builder().build()
                ));

        when(transactionEventRecordRepository
                .findAll(pageable))
                .thenReturn(page);

        Page<TransactionEventRecord> result =
                service.getAll(null, null, pageable);

        assertThat(result.getContent())
                .hasSize(1);

        verify(transactionEventRecordRepository)
                .findAll(pageable);
    }

    /*
     =========================================================
     FAILED EVENT LOOKUP SUCCESS
     =========================================================
     */
    @Test
    @DisplayName("Should return failed event")
    void shouldReturnFailedEvent() {

        UUID id = UUID.randomUUID();

        FailedTransactionEvent event =
                new FailedTransactionEvent();

        when(failedEventRepository.findById(id))
                .thenReturn(Optional.of(event));

        FailedTransactionEvent result =
                service.getFailedEvent(id);

        assertThat(result)
                .isNotNull();

        verify(failedEventRepository)
                .findById(id);

        assertThat(
                meterRegistry
                        .counter("transaction.observability.query.failed.events")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     FAILED EVENT NOT FOUND
     =========================================================
     */
    @Test
    @DisplayName("Should throw when failed event not found")
    void shouldThrowWhenFailedEventNotFound() {

        UUID id = UUID.randomUUID();

        when(failedEventRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getFailedEvent(id)
        )
                .isInstanceOf(FailedEventNotFoundException.class);
    }

    /*
     =========================================================
     EXPORT SUCCESS
     =========================================================
     */
    @Test
    @DisplayName("Should export transactions successfully")
    void shouldExportTransactionsSuccessfully() {

        Instant from =
                Instant.now().minusSeconds(3600);

        Instant to =
                Instant.now();

        List<TransactionEventRecord> records =
                List.of(
                        TransactionEventRecord.builder().build(),
                        TransactionEventRecord.builder().build()
                );

        when(transactionEventRecordRepository
                .findByRecordedAtBetween(from, to))
                .thenReturn(records);

        List<TransactionEventRecord> result =
                service.exportTransactions(from, to);

        assertThat(result)
                .hasSize(2);

        verify(transactionEventRecordRepository)
                .findByRecordedAtBetween(from, to);

        assertThat(
                meterRegistry
                        .counter("transaction.observability.export.success")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     INVALID EXPORT RANGE
     =========================================================
     */
    @Test
    @DisplayName("Should throw for invalid export range")
    void shouldThrowForInvalidExportRange() {

        Instant from =
                Instant.now();

        Instant to =
                Instant.now().minusSeconds(3600);

        assertThatThrownBy(() ->
                service.exportTransactions(from, to)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Invalid export date range"
                );

        assertThat(
                meterRegistry
                        .counter("transaction.observability.export.failed")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     EXPORT LIMIT EXCEEDED
     =========================================================
     */
    @Test
    @DisplayName("Should throw when export exceeds limit")
    void shouldThrowWhenExportLimitExceeded() {

        Instant from =
                Instant.now().minusSeconds(3600);

        Instant to =
                Instant.now();

        List<TransactionEventRecord> hugeList =
                IntStream.range(0, 10001)
                        .mapToObj(i ->
                                TransactionEventRecord.builder().build()
                        )
                        .toList();

        when(transactionEventRecordRepository
                .findByRecordedAtBetween(from, to))
                .thenReturn(hugeList);

        assertThatThrownBy(() ->
                service.exportTransactions(from, to)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Export exceeds maximum allowed records"
                );

        assertThat(
                meterRegistry
                        .counter("transaction.observability.export.failed")
                        .count()
        ).isEqualTo(1.0);
    }
}