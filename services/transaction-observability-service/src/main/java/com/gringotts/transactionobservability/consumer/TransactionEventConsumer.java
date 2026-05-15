package com.gringotts.transactionobservability.consumer;


import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.transactionobservability.service.FailedEventService;
import com.gringotts.transactionobservability.service.TransactionObservabilityProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final TransactionObservabilityProcessingService transactionObservabilityProcessingService;
    private final FailedEventService failedEventService;

    @KafkaListener(
            topics = "transaction.finalized.v1",
            groupId = "transaction-observability-event",//
            concurrency = "3"

    )
    public void consume(
            @Payload TransactionFinalizedEvent payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {

        log.info("Received event partition={} offset={}", partition, offset);

        try {

            // 1. Process business logic
            transactionObservabilityProcessingService.process(payload);

            // 2. ACK ONLY AFTER SUCCESS
            ack.acknowledge();

            log.info("ACK success offset={}", offset);

        } catch (IllegalArgumentException ex) {

            // 🚨 Poison message (bad JSON, schema issue)
            log.error("Invalid event. Sending to DLQ. offset={}", offset, ex);

            // Let error handler / DLQ handle it
            throw ex;

        } catch (Exception ex) {

            // 🚨 Transient failure (DB, network, etc.)
            log.error("Processing failed. Will retry. offset={}", offset, ex);

            // DO NOT ACK → Kafka will retry
            throw ex;
        }
    }
    @DltHandler
    @Transactional
    public void handleDlt(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage
    ) {

        log.error("DLQ EVENT topic={} reason={}", topic, errorMessage);

        failedEventService.persist(payload, errorMessage, topic);
    }
}