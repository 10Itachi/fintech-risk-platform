package com.gringotts.transactionobservability.service;

import com.gringotts.transactionobservability.domain.model.FailedTransactionEvent;
import com.gringotts.transactionobservability.repository.FailedEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class FailedEventService {

    /*
     =========================================================
     SAFETY LIMITS
     =========================================================
     */
    private static final int MAX_ERROR_LENGTH = 2000;
    private static final int MAX_PAYLOAD_LENGTH = 50_000;

    private final FailedEventRepository failedEventRepository;

    /*
     =========================================================
     METRICS
     =========================================================
     */
    private final Counter failedEventPersistCounter;
    private final Counter failedEventPersistenceFailureCounter;

    private final Timer failedEventPersistenceTimer;

    private final DistributionSummary failedPayloadSizeMetric;

    public FailedEventService(
            FailedEventRepository failedEventRepository,
            MeterRegistry meterRegistry
    ) {

        this.failedEventRepository =
                failedEventRepository;

        /*
         =====================================================
         COUNTERS
         =====================================================
         */
        this.failedEventPersistCounter =
                meterRegistry.counter(
                        "transaction.observability.failed.events.persisted"
                );

        this.failedEventPersistenceFailureCounter =
                meterRegistry.counter(
                        "transaction.observability.failed.events.persistence.failed"
                );

        /*
         =====================================================
         TIMERS
         =====================================================
         */
        this.failedEventPersistenceTimer =
                meterRegistry.timer(
                        "transaction.observability.failed.events.persistence.time"
                );

        /*
         =====================================================
         DISTRIBUTION
         =====================================================
         */
        this.failedPayloadSizeMetric =
                meterRegistry.summary(
                        "transaction.observability.failed.events.payload.bytes"
                );
    }

    @Transactional
    public void persist(
            String payload,
            String error,
            String topic
    ) {

        Timer.Sample timer = Timer.start();

        try {

            /*
             =================================================
             PAYLOAD PROTECTION
             =================================================
             */
            String safePayload = payload;

            if (safePayload != null
                    && safePayload.length() > MAX_PAYLOAD_LENGTH) {

                safePayload =
                        safePayload.substring(
                                0,
                                MAX_PAYLOAD_LENGTH
                        );

                log.warn(
                        "event=failed_payload_truncated topic={} maxPayloadLength={}",
                        topic,
                        MAX_PAYLOAD_LENGTH
                );
            }

            /*
             =================================================
             ERROR PROTECTION
             =================================================
             */
            String safeError = error;

            if (safeError != null
                    && safeError.length() > MAX_ERROR_LENGTH) {

                safeError =
                        safeError.substring(
                                0,
                                MAX_ERROR_LENGTH
                        );
            }

            /*
             =================================================
             METRICS
             =================================================
             */
            if (safePayload != null) {

                failedPayloadSizeMetric.record(
                        safePayload.getBytes().length
                );
            }

            /*
             =================================================
             ENTITY CREATION
             =================================================
             */
            FailedTransactionEvent entity =
                    new FailedTransactionEvent();

            entity.setPayload(safePayload);
            entity.setSourceTopic(topic);
            entity.setErrorMessage(safeError);
            entity.setFailedAt(Instant.now());

            failedEventRepository.save(entity);

            failedEventPersistCounter.increment();

            log.error(
                    "event=failed_event_persisted topic={} payloadSize={} error={}",
                    topic,
                    safePayload != null
                            ? safePayload.length()
                            : 0,
                    safeError
            );

        } catch (Exception ex) {

            failedEventPersistenceFailureCounter.increment();

            log.error(
                    "event=failed_event_persistence_failed topic={} message={}",
                    topic,
                    ex.getMessage(),
                    ex
            );

            throw ex;

        } finally {

            timer.stop(failedEventPersistenceTimer);
        }
    }
}