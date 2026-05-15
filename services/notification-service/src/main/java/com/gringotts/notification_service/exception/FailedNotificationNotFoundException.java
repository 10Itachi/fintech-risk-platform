package com.gringotts.notification_service.exception;

import java.util.UUID;

public class FailedNotificationNotFoundException
        extends RuntimeException {

    public FailedNotificationNotFoundException(UUID id) {

        super(
                "Failed notification event not found: " + id
        );
    }
}