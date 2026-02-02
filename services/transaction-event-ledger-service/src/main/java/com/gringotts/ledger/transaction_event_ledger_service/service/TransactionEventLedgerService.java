    package com.gringotts.ledger.transaction_event_ledger_service.service;

    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.gringotts.kafkaevents.TransactionFinalizedEvent;
    import com.gringotts.ledger.transaction_event_ledger_service.domain.model.TransactionEventLedgerEntity;
    import com.gringotts.ledger.transaction_event_ledger_service.repository.TransactionEventLedger;
    import jakarta.transaction.Transactional;
    import org.springframework.stereotype.Service;

    import java.time.Instant;
    import java.util.UUID;

    import static com.gringotts.ledger.transaction_event_ledger_service.service.ChecksumUtil.sha256;

    @Service
    public class TransactionEventLedgerService {
        private final TransactionEventLedger transactionEventLedgerRepository;
        private final ObjectMapper objectMapper;

        public TransactionEventLedgerService(TransactionEventLedger transactionEventLedgerRepository, ObjectMapper objectMapper) {
            this.transactionEventLedgerRepository = transactionEventLedgerRepository;
            this.objectMapper = objectMapper;
        }

        @Transactional
        public String recordEvent(TransactionFinalizedEvent event){
            String payloadJson;
            try{
                payloadJson = objectMapper.writeValueAsString(event);
            }catch (JsonProcessingException e){
                throw new RuntimeException("Unable to serialize TransactionFinalizedEvent", e);
            }
            TransactionEventLedgerEntity entity= TransactionEventLedgerEntity.builder()
                    .eventId(UUID.randomUUID())
                    .transactionId(event.transactionId())
                    .eventType("TRANSACTION_FINALIZED")
                    .eventVersion("v1")
                    .sourceService("transaction-service")
                    .eventPayload(payloadJson)
                    .checksum(sha256(payloadJson))
                    .occurredAt(event.occurredAt())
                    .recordedAt(Instant.now())
                    .build();
            transactionEventLedgerRepository.save(entity);
            return payloadJson;
        }
    }
