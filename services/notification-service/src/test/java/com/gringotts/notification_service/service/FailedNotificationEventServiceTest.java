package com.gringotts.notification_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.notification_service.entity.FailedNotificationEvent;
import com.gringotts.notification_service.repository.FailedNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FailedNotificationEventServiceTest {

    @Mock
    private FailedNotificationRepository failedNotificationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FailedNotificationEventService service;

    private String payload;

    private String topic;

    @BeforeEach
    void setUp() {

        payload = """
                {
                  "transactionId":"txn-123",
                  "status":"FAILED"
                }
                """;

        topic = "transaction.finalized.v1-dlt";
    }

    /*
     =========================================================
     SUCCESS FLOW
     =========================================================
     */
    @Test
    void shouldPersistFailedNotificationEvent() {

        String error = "Kafka deserialization failed";

        service.persist(payload, error, topic);

        ArgumentCaptor<FailedNotificationEvent> captor =
                ArgumentCaptor.forClass(FailedNotificationEvent.class);

        verify(failedNotificationRepository, times(1))
                .save(captor.capture());

        FailedNotificationEvent savedEntity =
                captor.getValue();

        assertThat(savedEntity.getPayload())
                .isEqualTo(payload);

        assertThat(savedEntity.getSourceTopic())
                .isEqualTo(topic);

        assertThat(savedEntity.getErrorMessage())
                .isEqualTo(error);

        assertThat(savedEntity.getFailedAt())
                .isNotNull();
    }

    /*
     =========================================================
     LONG ERROR SHOULD BE TRUNCATED
     =========================================================
     */
    @Test
    void shouldTruncateLongErrorMessage() {

        String longError =
                "X".repeat(3000);

        service.persist(payload, longError, topic);

        ArgumentCaptor<FailedNotificationEvent> captor =
                ArgumentCaptor.forClass(FailedNotificationEvent.class);

        verify(failedNotificationRepository)
                .save(captor.capture());

        FailedNotificationEvent savedEntity =
                captor.getValue();

        assertThat(savedEntity.getErrorMessage())
                .hasSize(2000);
    }

    /*
     =========================================================
     NULL ERROR SHOULD BE HANDLED
     =========================================================
     */
    @Test
    void shouldHandleNullErrorMessage() {

        service.persist(payload, null, topic);

        ArgumentCaptor<FailedNotificationEvent> captor =
                ArgumentCaptor.forClass(FailedNotificationEvent.class);

        verify(failedNotificationRepository)
                .save(captor.capture());

        FailedNotificationEvent savedEntity =
                captor.getValue();

        assertThat(savedEntity.getErrorMessage())
                .isNull();
    }

    /*
     =========================================================
     DATABASE FAILURE
     =========================================================
     */
    @Test
    void shouldThrowExceptionWhenDatabaseFails() {

        doThrow(new DataAccessResourceFailureException("DB unavailable"))
                .when(failedNotificationRepository)
                .save(any(FailedNotificationEvent.class));

        assertThatThrownBy(() ->
                service.persist(
                        payload,
                        "Some error",
                        topic
                )
        )
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("DB unavailable");
    }
}