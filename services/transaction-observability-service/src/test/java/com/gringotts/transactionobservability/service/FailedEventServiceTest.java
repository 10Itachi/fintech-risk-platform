package com.gringotts.transactionobservability.service;

import com.gringotts.transactionobservability.domain.model.FailedTransactionEvent;
import com.gringotts.transactionobservability.repository.FailedEventRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailedEventServiceTest {

    @Mock
    private FailedEventRepository failedEventRepository;

    @Captor
    private ArgumentCaptor<FailedTransactionEvent> captor;

    private FailedEventService service;

    private final SimpleMeterRegistry meterRegistry =
            new SimpleMeterRegistry();

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        service = new FailedEventService(
                failedEventRepository,
                meterRegistry
        );
    }

    /*
     =========================================================
     SUCCESS FLOW
     =========================================================
     */
    @Test
    @DisplayName("Should persist failed event successfully")
    void shouldPersistFailedEventSuccessfully() {

        String payload =
                "{\"status\":\"FAILED\"}";

        String error =
                "Kafka deserialization failure";

        String topic =
                "transaction.finalized.v1";

        service.persist(
                payload,
                error,
                topic
        );

        verify(failedEventRepository)
                .save(captor.capture());

        FailedTransactionEvent saved =
                captor.getValue();

        assertThat(saved.getPayload())
                .isEqualTo(payload);

        assertThat(saved.getErrorMessage())
                .isEqualTo(error);

        assertThat(saved.getSourceTopic())
                .isEqualTo(topic);

        assertThat(saved.getFailedAt())
                .isNotNull();

        assertThat(
                meterRegistry
                        .counter("transaction.observability.failed.events.persisted")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     PAYLOAD TRUNCATION
     =========================================================
     */
    @Test
    @DisplayName("Should truncate oversized payload")
    void shouldTruncateOversizedPayload() {

        String hugePayload =
                "A".repeat(60_000);

        service.persist(
                hugePayload,
                "failure",
                "topic"
        );

        verify(failedEventRepository)
                .save(captor.capture());

        FailedTransactionEvent saved =
                captor.getValue();

        assertThat(saved.getPayload().length())
                .isEqualTo(50_000);
    }

    /*
     =========================================================
     ERROR TRUNCATION
     =========================================================
     */
    @Test
    @DisplayName("Should truncate oversized error message")
    void shouldTruncateOversizedErrorMessage() {

        String hugeError =
                "X".repeat(3000);

        service.persist(
                "payload",
                hugeError,
                "topic"
        );

        verify(failedEventRepository)
                .save(captor.capture());

        FailedTransactionEvent saved =
                captor.getValue();

        assertThat(saved.getErrorMessage().length())
                .isEqualTo(2000);
    }

    /*
     =========================================================
     NULL PAYLOAD
     =========================================================
     */
    @Test
    @DisplayName("Should handle null payload safely")
    void shouldHandleNullPayload() {

        service.persist(
                null,
                "failure",
                "topic"
        );

        verify(failedEventRepository)
                .save(captor.capture());

        FailedTransactionEvent saved =
                captor.getValue();

        assertThat(saved.getPayload())
                .isNull();

        assertThat(
                meterRegistry
                        .counter("transaction.observability.failed.events.persisted")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     PERSISTENCE FAILURE
     =========================================================
     */
    @Test
    @DisplayName("Should increment failure metric on DB failure")
    void shouldIncrementFailureMetricOnPersistenceFailure() {

        doThrow(
                new DataAccessResourceFailureException(
                        "database unavailable"
                )
        )
                .when(failedEventRepository)
                .save(any(FailedTransactionEvent.class));

        assertThatThrownBy(() ->
                service.persist(
                        "payload",
                        "error",
                        "topic"
                )
        )
                .isInstanceOf(
                        DataAccessResourceFailureException.class
                )
                .hasMessageContaining(
                        "database unavailable"
                );

        assertThat(
                meterRegistry
                        .counter("transaction.observability.failed.events.persistence.failed")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     PAYLOAD SIZE METRIC
     =========================================================
     */
    @Test
    @DisplayName("Should record payload size metric")
    void shouldRecordPayloadSizeMetric() {

        String payload =
                "sample-payload";

        service.persist(
                payload,
                "error",
                "topic"
        );

        assertThat(
                meterRegistry
                        .summary("transaction.observability.failed.events.payload.bytes")
                        .count()
        ).isEqualTo(1);

        assertThat(
                meterRegistry
                        .summary("transaction.observability.failed.events.payload.bytes")
                        .totalAmount()
        ).isGreaterThan(0);
    }
}