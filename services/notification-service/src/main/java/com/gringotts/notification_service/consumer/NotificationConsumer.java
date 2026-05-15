package com.gringotts.notification_service.consumer;

import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.notification_service.service.FailedNotificationEventService;
import com.gringotts.notification_service.service.NotificationProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationProcessingService service;
    private final FailedNotificationEventService failedNotificationEventService;
    @KafkaListener(
            topics = "transaction.finalized.v1",
            groupId = "notification-group",
            concurrency = "2"
    )
    public void consume(
            @Payload TransactionFinalizedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {

        log.info("event=NOTIF_RECEIVED partition={} offset={} txnId={}",
                partition, offset, event.getTransactionId());

        try {
            service.process(event);   // <-- DTO now
            ack.acknowledge();
            log.info("event=NOTIF_ACK offset={}", offset);

        } catch (IllegalArgumentException ex) {
            // non-retryable → DLQ
            log.error("event=NOTIF_POISON offset={}", offset, ex);
            throw ex;

        } catch (Exception ex) {
            // retryable
            log.error("event=NOTIF_RETRY offset={}", offset, ex);
            throw ex;
        }
    }

    @DltHandler
    public void handleDlt(
            @Payload byte[] raw,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage
    ) {
        String payload = raw != null ? new String(raw) : null;
        log.error("DLQ event topic={} error={}", topic, errorMessage);
        failedNotificationEventService.persist(payload, errorMessage, topic);
    }
}
