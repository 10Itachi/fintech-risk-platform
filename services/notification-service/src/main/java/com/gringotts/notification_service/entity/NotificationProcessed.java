package com.gringotts.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notification_processed",

        indexes = {

                @Index(
                        name = "idx_processed_at",
                        columnList = "processed_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationProcessed {

    @Id
    @Column(
            name = "event_id",
            nullable = false,
            updatable = false,
            columnDefinition = "BINARY(16)"
    )
    private UUID eventId;

    @Column(
            name = "processed_at",
            nullable = false
    )
    private Instant processedAt;
}