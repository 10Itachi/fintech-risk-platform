package com.gringotts.ledger.transaction_event_ledger_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.ledger.transaction_event_ledger_service.domain.model.FailedTransactionEvent;
import com.gringotts.ledger.transaction_event_ledger_service.repository.FailedTransactionEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.time.Instant;

@Service
public class FailedEventService {
        Logger LOGGER = LoggerFactory.getLogger(FailedEventService.class);
    private final FailedTransactionEventRepository failedEventRepository;
    private final ObjectMapper  objectMapper;

    public FailedEventService(FailedTransactionEventRepository failedEventRepository, ObjectMapper objectMapper) {
        this.failedEventRepository = failedEventRepository;
        this.objectMapper = objectMapper;
    }
    @Transactional
    public void persist(TransactionFinalizedEvent event, String error, String topic) {
        FailedTransactionEvent failedTransactionEvent = new FailedTransactionEvent();
        failedTransactionEvent.setTransactionId(String.valueOf(event.transactionId()));
        failedTransactionEvent.setSourceTopic(topic);
        // Safety Truncation: Ensure we don't crash if the error is massive
        String truncatedError = (error != null && error.length() > 2000)
                ? error.substring(0, 2000)
                : error;
        failedTransactionEvent.setErrorMessage(error);
        failedTransactionEvent.setFailedAt(Instant.now());
        try {
            failedTransactionEvent.setPayload(objectMapper.writeValueAsString(event));
        }catch (Exception e){
            failedTransactionEvent.setPayload("SERIALIZATION_FAILED");
        }
        failedEventRepository.save(failedTransactionEvent);
        LOGGER.info("*****-Failed Transaction Event Persist Successfully*****");
    }
}
