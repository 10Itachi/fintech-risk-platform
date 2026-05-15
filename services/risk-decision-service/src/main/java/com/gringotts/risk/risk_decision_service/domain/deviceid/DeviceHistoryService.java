package com.gringotts.risk.risk_decision_service.domain.deviceid;

import com.gringotts.risk.risk_decision_service.infrastructure.repository.UserDeviceHistoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DeviceHistoryService {

    private final UserDeviceHistoryRepository repository;

    public DeviceHistoryService(UserDeviceHistoryRepository repository) {
        this.repository = repository;
    }

    public boolean isNewDeviceAndRecord(String userId, String deviceId, Instant now) {

        // Fast path
        if (repository.existsByUserIdAndDeviceId(userId, deviceId)) {

            repository.findByUserIdAndDeviceId(userId, deviceId)
                    .ifPresent(entity -> {
                        entity.updateLastSeen(now);
                        repository.save(entity);
                    });

            return false;
        }

        // First-time insert (race-safe)
        try {
            repository.save(new UserDeviceHistoryEntity(userId, deviceId, now));
            return true;
        } catch (DataIntegrityViolationException ex) {
            // Another request inserted same device concurrently
            return false;
        }
    }
}