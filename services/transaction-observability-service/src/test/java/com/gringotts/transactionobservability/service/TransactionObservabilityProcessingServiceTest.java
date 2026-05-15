package com.gringotts.transactionobservability.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.transactionobservability.domain.model.ProcessedEvent;
import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;
import com.gringotts.transactionobservability.repository.ProcessedEventRepository;
import com.gringotts.transactionobservability.repository.TransactionEventRecordRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionObservabilityProcessingServiceTest {

    @Mock
    private TransactionEventRecordRepository transactionEventRecordRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    private TransactionObservabilityProcessingService service;

    private final SimpleMeterRegistry meterRegistry =
            new SimpleMeterRegistry();

    @Captor
    private ArgumentCaptor<TransactionEventRecord> recordCaptor;

    @Captor
    private ArgumentCaptor<ProcessedEvent> processedCaptor;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        service = new TransactionObservabilityProcessingService(
                transactionEventRecordRepository,
                processedEventRepository,
                objectMapper,
                meterRegistry
        );
    }

    /*
     =========================================================
     SUCCESS FLOW
     =========================================================
     */
    @Test
    @DisplayName("Should process approved transaction successfully")
    void shouldProcessApprovedTransactionSuccessfully()
            throws Exception {

        TransactionFinalizedEvent event =
                buildApprovedEvent();

        when(processedEventRepository.existsByEventId(
                event.getEventId()))
                .thenReturn(false);

        when(objectMapper.writeValueAsString(event))
                .thenReturn("{\"status\":\"APPROVED\"}");

        service.process(event);

        verify(transactionEventRecordRepository)
                .save(recordCaptor.capture());

        TransactionEventRecord saved =
                recordCaptor.getValue();

        assertThat(saved.getTransactionId())
                .isEqualTo(event.getTransactionId());

        assertThat(saved.getEventId())
                .isEqualTo(event.getEventId());

        assertThat(saved.getChecksum())
                .isNotBlank();

        verify(processedEventRepository, times(1))
                .save(processedCaptor.capture());

        assertThat(processedCaptor.getValue()
                .getEventId())
                .isEqualTo(event.getEventId());

        assertThat(
                meterRegistry
                        .counter("transaction.observability.events.processed")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     DUPLICATE EVENT
     =========================================================
     */
    @Test
    @DisplayName("Should skip duplicate event")
    void shouldSkipDuplicateEvent() throws JsonProcessingException {

        TransactionFinalizedEvent event =
                buildApprovedEvent();

        when(processedEventRepository.existsByEventId(
                event.getEventId()))
                .thenReturn(true);

        service.process(event);

        verifyNoInteractions(
                transactionEventRecordRepository
        );

        verify(objectMapper, never())
                .writeValueAsString(any());

        assertThat(
                meterRegistry
                        .counter("transaction.observability.events.duplicate")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     NON APPROVED EVENT
     =========================================================
     */
    @Test
    @DisplayName("Should skip non approved transaction")
    void shouldSkipNonApprovedTransaction()
            throws Exception {

        TransactionFinalizedEvent event =
                buildApprovedEvent();

        event.setFinalStatus(
                TransactionStatus.REVIEW
        );

        when(processedEventRepository.existsByEventId(
                event.getEventId()))
                .thenReturn(false);

        service.process(event);

        verifyNoInteractions(
                transactionEventRecordRepository
        );

        verify(processedEventRepository)
                .save(any(ProcessedEvent.class));

        verify(objectMapper, never())
                .writeValueAsString(any());

        assertThat(
                meterRegistry
                        .counter("transaction.observability.events.skipped")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     SERIALIZATION FAILURE
     =========================================================
     */
    @Test
    @DisplayName("Should throw exception when serialization fails")
    void shouldThrowWhenSerializationFails()
            throws Exception {

        TransactionFinalizedEvent event =
                buildApprovedEvent();

        when(processedEventRepository.existsByEventId(
                event.getEventId()))
                .thenReturn(false);

        when(objectMapper.writeValueAsString(event))
                .thenThrow(
                        new JsonProcessingException("serialization failed") {
                        }
                );

        assertThatThrownBy(() ->
                service.process(event)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(
                        "Failed to serialize event payload"
                );

        verifyNoInteractions(
                transactionEventRecordRepository
        );

        assertThat(
                meterRegistry
                        .counter("transaction.observability.events.failed")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     DATABASE DUPLICATE
     =========================================================
     */
    @Test
    @DisplayName("Should handle duplicate DB record gracefully")
    void shouldHandleDuplicateDatabaseRecord()
            throws Exception {

        TransactionFinalizedEvent event =
                buildApprovedEvent();

        when(processedEventRepository.existsByEventId(
                event.getEventId()))
                .thenReturn(false);

        when(objectMapper.writeValueAsString(event))
                .thenReturn("{\"status\":\"APPROVED\"}");

        when(transactionEventRecordRepository.save(any()))
                .thenThrow(
                        new DataIntegrityViolationException(
                                "duplicate"
                        )
                );

        service.process(event);

        verify(processedEventRepository, never())
                .save(any(ProcessedEvent.class));

        assertThat(
                meterRegistry
                        .counter("transaction.observability.events.duplicate")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     DATABASE FAILURE
     =========================================================
     */
    @Test
    @DisplayName("Should rethrow unexpected DB exception")
    void shouldRethrowUnexpectedDatabaseException()
            throws Exception {

        TransactionFinalizedEvent event =
                buildApprovedEvent();

        when(processedEventRepository.existsByEventId(
                event.getEventId()))
                .thenReturn(false);

        when(objectMapper.writeValueAsString(event))
                .thenReturn("{\"status\":\"APPROVED\"}");

        when(transactionEventRecordRepository.save(any()))
                .thenThrow(
                        new RuntimeException("database down")
                );

        assertThatThrownBy(() ->
                service.process(event)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("database down");

        assertThat(
                meterRegistry
                        .counter("transaction.observability.events.failed")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     TEST DATA
     =========================================================
     */
    private TransactionFinalizedEvent buildApprovedEvent() {

        return TransactionFinalizedEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(UUID.randomUUID())
                .eventType("TRANSACTION_FINALIZED")
                .eventVersion(1)
                .occurredAt(Instant.now())
                .userId(String.valueOf(UUID.randomUUID()))
                .amount(BigDecimal.valueOf(1000))
                .finalStatus(TransactionStatus.APPROVED)
                .build();
    }
}