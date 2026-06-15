package com.gringotts.notification_service.repository;
import com.gringotts.notification_service.entity.FailedNotificationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface FailedNotificationRepository extends JpaRepository<FailedNotificationEvent, UUID> {
    Page<FailedNotificationEvent> findByFailedAtBetween(
            Instant from,
            Instant to,
            Pageable pageable
    );
}
