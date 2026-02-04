package com.gringotts.ledger.transaction_event_ledger_service.consumer;

import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.ledger.transaction_event_ledger_service.service.FailedEventService;
import com.gringotts.ledger.transaction_event_ledger_service.service.TransactionEventLedgerService;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("transactionEventConsumer")
public class TransactionEventConsumer {

    private final Logger LOGGER = LoggerFactory.getLogger(TransactionEventConsumer.class);
    private final TransactionEventLedgerService ledgerService;
    private final FailedEventService failedEventService;

    public TransactionEventConsumer(TransactionEventLedgerService ledgerService, FailedEventService failedEventService) {
        this.ledgerService = ledgerService;
        this.failedEventService = failedEventService;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000),
            // Removed fixedDelayTopicStrategy line
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            exclude = {
                    MessageConversionException.class,
                    IllegalArgumentException.class
            }
    )
    @KafkaListener(topics = "${spring.kafka.topics.transaction-finalized}")
    public void handler(
            @Payload TransactionFinalizedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        // Banking Logic: Simulated Failure for PK
        if (event.country() != null && event.country().equals("PK")) {
            LOGGER.error("!!! Triggering Failure for Country PK !!!");
            throw new RuntimeException("Simulated Business Logic Failure!");
        }

        LOGGER.info("****** Received TransactionFinalizedEvent. ID: {}", event.transactionId());
        ledgerService.recordEvent(event);

        // Manual ACK is critical in banking to ensure we don't lose messages
        ack.acknowledge();
    }

    @DltHandler
    @Transactional
    public void handleDlt(
            TransactionFinalizedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage
    ) {
        LOGGER.error("❌ DLT EVENT received for Topic: {}. Reason: {}", topic, errorMessage);
        failedEventService.persist(event, errorMessage, topic);
    }
}