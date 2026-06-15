package com.gringotts.notification_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.notification_service.entity.FailedNotificationEvent;
import com.gringotts.notification_service.repository.FailedNotificationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class FailedNotificationEventService {
        Logger LOGGER = LoggerFactory.getLogger(FailedNotificationEventService.class);
    private final FailedNotificationRepository failedEventRepository;
    private final ObjectMapper  objectMapper;

    public FailedNotificationEventService(FailedNotificationRepository failedEventRepository, ObjectMapper objectMapper) {
        this.failedEventRepository = failedEventRepository;
        this.objectMapper = objectMapper;
    }
    @Transactional
    public void persist(String payload, String error, String topic) {

        FailedNotificationEvent entity = new FailedNotificationEvent();

        entity.setPayload(payload);
        entity.setSourceTopic(topic);

        String truncatedError = (error != null && error.length() > 2000)
                ? error.substring(0, 2000)
                : error;

        entity.setErrorMessage(truncatedError);
        entity.setFailedAt(Instant.now());

        failedEventRepository.save(entity);

        log.info("Failed event persisted");
    }
}
