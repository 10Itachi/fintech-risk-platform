package com.gringotts.notification_service.service;

import com.gringotts.enums.TransactionStatus;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class EmailNotificationSenderTest {

    @Mock
    private SesEmailClient sesEmailClient;

    @InjectMocks
    private EmailNotificationSender emailNotificationSender;

    private TransactionFinalizedEvent event;

    @BeforeEach
    void setUp() {

        event = TransactionFinalizedEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(UUID.randomUUID())
                .eventType("TRANSACTION_FINALIZED")
                .eventVersion(1)
                .occurredAt(Instant.now())
                .userId(UUID.randomUUID().toString())
                .userName("sangmesh")
                .email("test@gmail.com")
                .amount(BigDecimal.valueOf(5000))
                .finalStatus(TransactionStatus.APPROVED)
                .build();
    }

    /*
     =========================================================
     SUCCESS FLOW
     =========================================================
     */
    @Test
    void shouldSendEmailNotification() {

        emailNotificationSender.send(event);

        verify(sesEmailClient, times(1))
                .sendEmail(
                        eq(event.getEmail()),
                        eq("Transaction Status Update"),
                        contains("Hello sangmesh")
                );
    }

    /*
     =========================================================
     SHOULD PASS TRANSACTION DETAILS
     =========================================================
     */
    @Test
    void shouldPassCorrectTransactionDetailsToEmailClient() {

        emailNotificationSender.send(event);

        verify(sesEmailClient).sendEmail(
                eq("test@gmail.com"),
                eq("Transaction Status Update"),
                argThat(body ->
                        body.contains(event.getTransactionId().toString())
                                && body.contains(event.getAmount().toString())
                                && body.contains(event.getFinalStatus().toString())
                )
        );
    }

    /*
     =========================================================
     EMAIL CLIENT FAILURE
     =========================================================
     */
    @Test
    void shouldPropagateExceptionWhenSesFails() {

        doThrow(new RuntimeException("SES unavailable"))
                .when(sesEmailClient)
                .sendEmail(anyString(), anyString(), anyString());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        emailNotificationSender.send(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SES unavailable");

        verify(sesEmailClient, times(1))
                .sendEmail(anyString(), anyString(), anyString());
    }
}