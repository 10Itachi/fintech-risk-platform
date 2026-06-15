package com.gringotts.notification_service.service;

import com.gringotts.notification_service.entity.FailedNotificationEvent;
import com.gringotts.notification_service.entity.NotificationProcessed;
import com.gringotts.notification_service.exception.FailedNotificationNotFoundException;
import com.gringotts.notification_service.exception.NotificationEventNotFoundException;
import com.gringotts.notification_service.repository.FailedNotificationRepository;
import com.gringotts.notification_service.repository.NotificationProcessedRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class NotificationOperationsServiceTest {

    @Mock
    private FailedNotificationRepository failedRepository;

    @Mock
    private NotificationProcessedRepository processedRepository;

    @Mock
    private NotificationSender notificationSender;

    private NotificationOperationsService service;

    private UUID failedId;

    private UUID processedEventId;

    private FailedNotificationEvent failedEvent;

    private NotificationProcessed processedEvent;

    @BeforeEach
    void setUp() {

        service = new NotificationOperationsService(
                failedRepository,
                processedRepository,
                notificationSender,
                new SimpleMeterRegistry()
        );

        failedId = UUID.randomUUID();
        processedEventId = UUID.randomUUID();

        failedEvent = new FailedNotificationEvent();
        failedEvent.setId(failedId);
        failedEvent.setPayload("{json}");
        failedEvent.setErrorMessage("SMTP failed");
        failedEvent.setFailedAt(Instant.now());

        processedEvent = NotificationProcessed.builder()
                .eventId(processedEventId)
                .processedAt(Instant.now())
                .build();
    }

    /*
     =========================================================
     GET FAILED NOTIFICATIONS
     =========================================================
     */
    @Test
    void shouldGetFailedNotifications() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<FailedNotificationEvent> page =
                new PageImpl<>(List.of(failedEvent));

        when(failedRepository.findAll(pageable))
                .thenReturn(page);

        Page<FailedNotificationEvent> result =
                service.getFailedNotifications(
                        null,
                        null,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);

        verify(failedRepository, times(1))
                .findAll(pageable);
    }

    /*
     =========================================================
     GET FAILED NOTIFICATION BY ID
     =========================================================
     */
    @Test
    void shouldGetFailedNotificationById() {

        when(failedRepository.findById(failedId))
                .thenReturn(Optional.of(failedEvent));

        FailedNotificationEvent result =
                service.getFailedNotification(failedId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(failedId);
    }

    /*
     =========================================================
     FAILED NOTIFICATION NOT FOUND
     =========================================================
     */
    @Test
    void shouldThrowExceptionWhenFailedNotificationNotFound() {

        when(failedRepository.findById(failedId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getFailedNotification(failedId)
        )
                .isInstanceOf(FailedNotificationNotFoundException.class);
    }

    /*
     =========================================================
     GET PROCESSED EVENT
     =========================================================
     */
    @Test
    void shouldGetProcessedNotificationEvent() {

        when(processedRepository.findById(processedEventId))
                .thenReturn(Optional.of(processedEvent));

        NotificationProcessed result =
                service.getProcessedNotificationEvent(processedEventId);

        assertThat(result).isNotNull();
        assertThat(result.getEventId())
                .isEqualTo(processedEventId);
    }

    /*
     =========================================================
     PROCESSED EVENT NOT FOUND
     =========================================================
     */
    @Test
    void shouldThrowExceptionWhenProcessedEventNotFound() {

        when(processedRepository.findById(processedEventId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getProcessedNotificationEvent(processedEventId)
        )
                .isInstanceOf(NotificationEventNotFoundException.class);
    }

    /*
     =========================================================
     RETRY FAILED NOTIFICATION SUCCESS
     =========================================================
     */
    @Test
    void shouldRetryFailedNotificationSuccessfully() {

        when(failedRepository.findById(failedId))
                .thenReturn(Optional.of(failedEvent));

        assertThatCode(() ->
                service.retryFailedNotification(failedId)
        ).doesNotThrowAnyException();
    }

    /*
     =========================================================
     RETRY FAILED NOTIFICATION NOT FOUND
     =========================================================
     */
    @Test
    void shouldThrowExceptionWhenRetryEventNotFound() {

        when(failedRepository.findById(failedId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.retryFailedNotification(failedId)
        )
                .isInstanceOf(FailedNotificationNotFoundException.class);
    }
}