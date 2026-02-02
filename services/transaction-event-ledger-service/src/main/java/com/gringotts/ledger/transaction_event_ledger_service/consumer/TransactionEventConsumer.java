package com.gringotts.ledger.transaction_event_ledger_service.consumer;

import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.ledger.transaction_event_ledger_service.service.TransactionEventLedgerService;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Component
@KafkaListener(topics = "${spring.kafka.topics.transaction-finalized}")
public class TransactionEventConsumer {

    private final Logger LOGGER = LoggerFactory.getLogger(TransactionEventConsumer.class);
    private final TransactionEventLedgerService ledgerService;

    public TransactionEventConsumer(TransactionEventLedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @KafkaHandler
    public void handler(
            @Payload TransactionFinalizedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment ack
    ) {
        if (topic.endsWith("-dlt")) {
            LOGGER.error("DLT message received, persisting failure");
            // save to FAILED_EVENTS table
            ack.acknowledge();
            return;
        }

        if (event.country() != null && event.country().equals("PK")) {
            LOGGER.error("!!! Triggering Failure for Country PK !!!");
            throw new RuntimeException("Simulated Business Logic Failure!");
        }
            LOGGER.info("****** Received TransactionFinalizedEvent from Kafka");
            LOGGER.info("TransactionId : {}", event.transactionId());
            LOGGER.info("UserID : {}", event.userId());
            LOGGER.info("FinalStatus : {}", event.finalStatus());
        ledgerService.recordEvent(event);
        ack.acknowledge();
    }

    //@DltHandler
    public void handleDlt(
            @Payload TransactionFinalizedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error
    ) {
        LOGGER.error("❌ DLT message received from {}", topic);
        LOGGER.error("❌ Reason: {}", error);
        LOGGER.error("❌ TransactionId: {}", event.transactionId());

        // persist to FAILED_EVENTS table
    }


    @KafkaHandler(isDefault = true)
    public void defaultHandler(Object payload) {
        LOGGER.error("No handler found for payload type: {}", payload.getClass());
    }
}
