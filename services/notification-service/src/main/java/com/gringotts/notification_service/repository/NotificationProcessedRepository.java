package com.gringotts.notification_service.repository;

import com.gringotts.notification_service.entity.NotificationProcessed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationProcessedRepository
        extends JpaRepository<NotificationProcessed, UUID> {

    boolean existsByEventId(UUID eventId);
}
