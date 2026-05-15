package com.gringotts.notification_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.notification_service.entity.NotificationProcessed;
import com.gringotts.notification_service.repository.NotificationProcessedRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class NotificationProcessingServiceTest {

    @Mock
    private NotificationProcessedRepository processedRepo;

    @Mock
    private NotificationSender sender;

    @Mock
    private ObjectMapper objectMapper;

    private NotificationProcessingService service;

    private TransactionFinalizedEvent approvedEvent;

    private TransactionFinalizedEvent declinedEvent;

    @BeforeEach
    void setUp() {

        service = new NotificationProcessingService(
                processedRepo,
                sender,
                objectMapper,
                new SimpleMeterRegistry()
        );

        approvedEvent = TransactionFinalizedEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(UUID.randomUUID())
                .eventType("TRANSACTION_FINALIZED")
                .eventVersion(1)
                .occurredAt(Instant.now())
                .userId(UUID.randomUUID().toString())
                .userName("sangmesh")
                .email("test@gmail.com")
                .amount(BigDecimal.valueOf(1000))
                .finalStatus(TransactionStatus.APPROVED)
                .build();

        declinedEvent = TransactionFinalizedEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(UUID.randomUUID())
                .eventType("TRANSACTION_FINALIZED")
                .eventVersion(1)
                .occurredAt(Instant.now())
                .userId(UUID.randomUUID().toString())
                .userName("sangmesh")
                .email("test@gmail.com")
                .amount(BigDecimal.valueOf(1000))
                .finalStatus(TransactionStatus.DECLINED)
                .build();
    }

    /*
     =========================================================
     SUCCESS FLOW
     =========================================================
     */
    @Test
    void shouldProcessApprovedNotificationSuccessfully() {

        when(processedRepo.existsByEventId(
                approvedEvent.getEventId()
        )).thenReturn(false);

        service.process(approvedEvent);

        verify(sender, times(1))
                .send(approvedEvent);

        verify(processedRepo, times(1))
                .save(any(NotificationProcessed.class));
    }

    /*
     =========================================================
     DUPLICATE EVENT
     =========================================================
     */
    @Test
    void shouldSkipDuplicateEvent() {

        when(processedRepo.existsByEventId(
                approvedEvent.getEventId()
        )).thenReturn(true);

        service.process(approvedEvent);

        verify(sender, never())
                .send(any());

        verify(processedRepo, never())
                .save(any());
    }

    /*
     =========================================================
     NON APPROVED EVENT
     =========================================================
     */
    @Test
    void shouldSkipNonApprovedEvent() {

        when(processedRepo.existsByEventId(
                declinedEvent.getEventId()
        )).thenReturn(false);

        service.process(declinedEvent);

        verify(sender, never())
                .send(any());

        verify(processedRepo, times(1))
                .save(any(NotificationProcessed.class));
    }

    /*
     =========================================================
     NOTIFICATION FAILURE
     =========================================================
     */
    @Test
    void shouldThrowExceptionWhenNotificationFails() {

        when(processedRepo.existsByEventId(
                approvedEvent.getEventId()
        )).thenReturn(false);

        doThrow(new RuntimeException("SMTP failure"))
                .when(sender)
                .send(any());

        assertThatThrownBy(() ->
                service.process(approvedEvent)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP failure");

        verify(processedRepo, never())
                .save(any(NotificationProcessed.class));
    }

    /*
     =========================================================
     DB RACE CONDITION SAFE
     =========================================================
     */
    @Test
    void shouldHandleDuplicateInsertGracefully() {

        when(processedRepo.existsByEventId(
                approvedEvent.getEventId()
        )).thenReturn(false);

        doNothing()
                .when(sender)
                .send(any());

        doThrow(DataIntegrityViolationException.class)
                .when(processedRepo)
                .save(any(NotificationProcessed.class));

        assertThatCode(() ->
                service.process(approvedEvent)
        ).doesNotThrowAnyException();

        verify(sender, times(1))
                .send(any());
    }
}