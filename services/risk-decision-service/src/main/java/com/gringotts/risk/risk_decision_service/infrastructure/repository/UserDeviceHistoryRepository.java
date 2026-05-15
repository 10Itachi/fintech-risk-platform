package com.gringotts.risk.risk_decision_service.infrastructure.repository;

import com.gringotts.risk.risk_decision_service.domain.deviceid.UserDeviceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDeviceHistoryRepository
        extends JpaRepository<UserDeviceHistoryEntity, Long> {

    boolean existsByUserIdAndDeviceId(String userId, String deviceId);

    Optional<UserDeviceHistoryEntity> findByUserIdAndDeviceId(
            String userId,
            String deviceId
    );
}