package com.gringotts.risk.risk_decision_service.domain.deviceid;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "user_device_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_device",
                columnNames = {"userId", "deviceId"}
        )
)
@Getter
@NoArgsConstructor
public class UserDeviceHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String deviceId;

    private Instant firstSeenAt;
    private Instant lastSeenAt;

    public UserDeviceHistoryEntity(String userId, String deviceId, Instant now) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.firstSeenAt = now;
        this.lastSeenAt = now;
    }

    public void updateLastSeen(Instant now) {
        this.lastSeenAt = now;
    }
}