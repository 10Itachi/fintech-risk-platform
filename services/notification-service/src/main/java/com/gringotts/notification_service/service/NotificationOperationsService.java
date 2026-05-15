package com.gringotts.notification_service.service;

import com.gringotts.notification_service.entity.FailedNotificationEvent;
import com.gringotts.notification_service.entity.NotificationProcessed;
import com.gringotts.notification_service.exception.FailedNotificationNotFoundException;
import com.gringotts.notification_service.exception.NotificationEventNotFoundException;
import com.gringotts.notification_service.repository.FailedNotificationRepository;
import com.gringotts.notification_service.repository.NotificationProcessedRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class NotificationOperationsService {

    private final FailedNotificationRepository failedRepository;
    private final NotificationProcessedRepository processedRepository;
    private final NotificationSender notificationSender;

    /*
     =========================================================
     METRICS
     =========================================================
     */
    private final Counter failedQueryCounter;
    private final Counter retryCounter;
    private final Counter retryFailureCounter;
    private final Counter processedQueryCounter;

    public NotificationOperationsService(
            FailedNotificationRepository failedRepository,
            NotificationProcessedRepository processedRepository,
            NotificationSender notificationSender,
            MeterRegistry meterRegistry
    ) {

        this.failedRepository = failedRepository;
        this.processedRepository = processedRepository;
        this.notificationSender = notificationSender;

        this.failedQueryCounter =
                meterRegistry.counter(
                        "notification.operations.failed.query"
                );

        this.retryCounter =
                meterRegistry.counter(
                        "notification.operations.retry.success"
                );

        this.retryFailureCounter =
                meterRegistry.counter(
                        "notification.operations.retry.failed"
                );

        this.processedQueryCounter =
                meterRegistry.counter(
                        "notification.operations.processed.query"
                );
    }

    /*
     =========================================================
     GET FAILED NOTIFICATIONS
     =========================================================
     */
    public Page<FailedNotificationEvent> getFailedNotifications(
            Instant from,
            Instant to,
            Pageable pageable
    ) {

        log.info(
                "event=FAILED_NOTIFICATION_QUERY from={} to={} page={} size={}",
                from,
                to,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        failedQueryCounter.increment();

        if (from != null && to != null) {

            return failedRepository.findByFailedAtBetween(
                    from,
                    to,
                    pageable
            );
        }

        return failedRepository.findAll(pageable);
    }

    /*
     =========================================================
     GET FAILED NOTIFICATION BY ID
     =========================================================
     */
    public FailedNotificationEvent getFailedNotification(
            UUID id
    ) {

        log.info(
                "event=FAILED_NOTIFICATION_FETCH id={}",
                id
        );

        failedQueryCounter.increment();

        return failedRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "event=FAILED_NOTIFICATION_NOT_FOUND id={}",
                            id
                    );

                    return new FailedNotificationNotFoundException(id);
                });
    }

    /*
     =========================================================
     GET PROCESSED NOTIFICATION EVENT
     =========================================================
     */
    public NotificationProcessed getProcessedNotificationEvent(
            UUID eventId
    ) {

        log.info(
                "event=PROCESSED_NOTIFICATION_FETCH eventId={}",
                eventId
        );

        processedQueryCounter.increment();

        return processedRepository.findById(eventId)
                .orElseThrow(() -> {

                    log.warn(
                            "event=PROCESSED_NOTIFICATION_NOT_FOUND eventId={}",
                            eventId
                    );

                    return new NotificationEventNotFoundException(
                            eventId
                    );
                });
    }

    /*
     =========================================================
     RETRY FAILED NOTIFICATION
     =========================================================
     */
    @Transactional
    public void retryFailedNotification(
            UUID failedEventId
    ) {

        log.info(
                "event=FAILED_NOTIFICATION_RETRY_STARTED id={}",
                failedEventId
        );

        FailedNotificationEvent failedEvent =
                failedRepository.findById(failedEventId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "event=FAILED_NOTIFICATION_NOT_FOUND id={}",
                                    failedEventId
                            );

                            return new FailedNotificationNotFoundException(
                                    failedEventId
                            );
                        });

        try {

            /*
             =================================================
             REAL RETRY LOGIC PLACEHOLDER
             =================================================

             Later:
             - deserialize payload
             - rebuild event
             - call notificationSender.send(...)
            */

            log.info(
                    "event=FAILED_NOTIFICATION_RETRY_SUCCESS id={}",
                    failedEventId
            );

            retryCounter.increment();

        } catch (Exception ex) {

            retryFailureCounter.increment();

            log.error(
                    "event=FAILED_NOTIFICATION_RETRY_FAILED id={}",
                    failedEventId,
                    ex
            );

            throw ex;
        }
    }
}