package com.gringotts.notification_service.exception;

import java.util.UUID;

public class NotificationEventNotFoundException
        extends RuntimeException {

    public NotificationEventNotFoundException(UUID eventId) {

        super(
                "Processed notification event not found: " + eventId
        );
    }
}