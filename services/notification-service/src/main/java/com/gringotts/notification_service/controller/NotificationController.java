package com.gringotts.notification_service.controller;

import com.gringotts.notification_service.entity.FailedNotificationEvent;
import com.gringotts.notification_service.entity.NotificationProcessed;
import com.gringotts.notification_service.service.NotificationOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationOperationsService notificationOperationsService;

    /*
     =========================================================
     GET FAILED NOTIFICATIONS
     =========================================================
     */
    @GetMapping("/failed")
    public ResponseEntity<Page<FailedNotificationEvent>> getFailedNotifications(

            @RequestParam(required = false)
            Instant from,

            @RequestParam(required = false)
            Instant to,

            @PageableDefault(
                    size = 20,
                    sort = "failedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<FailedNotificationEvent> response =
                notificationOperationsService.getFailedNotifications(
                        from,
                        to,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    /*
     =========================================================
     GET FAILED NOTIFICATION BY ID
     =========================================================
     */
    @GetMapping("/failed/{id}")
    public ResponseEntity<FailedNotificationEvent> getFailedNotification(

            @PathVariable
            UUID id
    ) {

        FailedNotificationEvent response =
                notificationOperationsService.getFailedNotification(id);

        return ResponseEntity.ok(response);
    }

    /*
     =========================================================
     RETRY FAILED NOTIFICATION
     =========================================================
     */
    @PostMapping("/failed/{id}/retry")
    public ResponseEntity<Map<String, Object>> retryFailedNotification(

            @PathVariable
            UUID id
    ) {

        notificationOperationsService.retryFailedNotification(id);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        Map.of(
                                "status", "RETRY_TRIGGERED",
                                "failedEventId", id,
                                "timestamp", Instant.now()
                        )
                );
    }

    /*
     =========================================================
     GET PROCESSED NOTIFICATION EVENT
     =========================================================
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<NotificationProcessed> getProcessedNotificationEvent(

            @PathVariable
            UUID eventId
    ) {

        NotificationProcessed response =
                notificationOperationsService
                        .getProcessedNotificationEvent(eventId);

        return ResponseEntity.ok(response);
    }
}