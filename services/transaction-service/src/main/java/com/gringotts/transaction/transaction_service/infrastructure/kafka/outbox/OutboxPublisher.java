package com.gringotts.transaction.transaction_service.infrastructure.kafka.outbox;


import com.gringotts.enums.OutboxStatus;
import com.gringotts.transaction.transaction_service.domain.model.OutboxEvent;
import com.gringotts.transaction.transaction_service.infrastructure.kafka.producer.KafkaProducerService;
import com.gringotts.transaction.transaction_service.infrastructure.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducerService kafkaProducerService;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaProducerService kafkaProducerService) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Value("${outbox.batch-size}")
    private int batchSize;

    @Value("${outbox.max-retry}")
    private int maxRetry;

    @Scheduled(fixedDelayString="${outbox.scheduler-delay-ms}")
    public void publish() {
        List<OutboxEvent> events = outboxEventRepository.findBatchForPublishing(
                List.of(OutboxStatus.PENDING,OutboxStatus.FAILED),
                Instant.now(),
                PageRequest.of(0, batchSize)
        );


        if (events.isEmpty()) {
            return;
        }
        log.info("Outbox batch size={}", events.size());
        for(OutboxEvent event : events) {
            processEvent(event);
        }
    }

    @Transactional
    public void processEvent(OutboxEvent event) {
        try{
            log.info("Publishing eventId={} txnId={}",
                    event.getId(), event.getAggregateId());
            kafkaProducerService.send(event.getAggregateId().toString(), event.getPayload(), event.getCorrelationId());
            event.setStatus(OutboxStatus.SENT);
            event.setSentAt(Instant.now());

            outboxEventRepository.save(event);

            log.info("SUCCESS eventId={}", event.getId());

        } catch (Exception ex) {
            handleFailure(event, ex);
        }
        }
    private void handleFailure(OutboxEvent event, Exception ex) {

        int retry = event.getRetryCount() + 1;
        event.setRetryCount(retry);

        if (retry >= maxRetry) {
            event.setStatus(OutboxStatus.DEAD);
            log.error("DEAD eventId={}", event.getId(), ex);
        } else {
            event.setStatus(OutboxStatus.FAILED);
            event.setNextRetryAt(
                    Instant.now().plusSeconds((long) Math.pow(2, retry))
            );
            log.warn("Retry eventId={} retryCount={}", event.getId(), retry);
        }

        outboxEventRepository.save(event);
    }
}

