package test.com.gringotts.transaction.transaction_service.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.enums.OutboxStatus;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.transaction.transaction_service.application.service.OutboxService;
import com.gringotts.transaction.transaction_service.domain.exception.OutboxSerializationException;
import com.gringotts.transaction.transaction_service.domain.model.OutboxEvent;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxService outboxService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {

        transaction = new Transaction();

        transaction.setTransactionId(UUID.randomUUID());

        transaction.setUserId(UUID.randomUUID());

        transaction.setUserName("Sangmesh");

        transaction.setEmail("test@gmail.com");

        transaction.setAmount(BigDecimal.valueOf(5000));

        transaction.setTransactionStatus(
                TransactionStatus.APPROVED
        );
    }

    /*
     =========================================================
     SUCCESS CASE
     =========================================================
     */
    @Test
    void shouldBuildTransactionFinalizedOutboxEventSuccessfully()
            throws Exception {

        String payload =
                """
                {
                    "eventType":"TRANSACTION_FINALIZED"
                }
                """;

        when(objectMapper.writeValueAsString(any(
                TransactionFinalizedEvent.class
        ))).thenReturn(payload);

        OutboxEvent result =
                outboxService.buildTransactionFinalizedEvent(
                        transaction
                );

        assertThat(result).isNotNull();

        assertThat(result.getId())
                .isNotNull();

        assertThat(result.getAggregateId())
                .isEqualTo(transaction.getTransactionId());

        assertThat(result.getAggregateType())
                .isEqualTo("TRANSACTION");

        assertThat(result.getEventType())
                .isEqualTo("TRANSACTION_FINALIZED");

        assertThat(result.getEventVersion())
                .isEqualTo(1);

        assertThat(result.getPayload())
                .isEqualTo(payload);

        assertThat(result.getStatus())
                .isEqualTo(OutboxStatus.PENDING);

        assertThat(result.getRetryCount())
                .isEqualTo(0);

        assertThat(result.getCreatedAt())
                .isNotNull();

        assertThat(result.getNextRetryAt())
                .isNotNull();

        verify(objectMapper, times(1))
                .writeValueAsString(any(
                        TransactionFinalizedEvent.class
                ));
    }

    /*
     =========================================================
     SERIALIZATION FAILURE
     =========================================================
     */
    @Test
    void shouldThrowOutboxSerializationExceptionWhenJsonFails()
            throws Exception {

        when(objectMapper.writeValueAsString(any(
                TransactionFinalizedEvent.class
        ))).thenThrow(
                new JsonProcessingException("Serialization failed") {
                }
        );

        assertThatThrownBy(() ->
                outboxService.buildTransactionFinalizedEvent(
                        transaction
                )
        )
                .isInstanceOf(
                        OutboxSerializationException.class
                )
                .hasMessageContaining(
                        "Outbox Failed to serialize"
                );

        verify(objectMapper, times(1))
                .writeValueAsString(any(
                        TransactionFinalizedEvent.class
                ));
    }

    /*
     =========================================================
     NULL TRANSACTION
     =========================================================
     */
    @Test
    void shouldThrowExceptionWhenTransactionIsNull() {

        assertThatThrownBy(() ->
                outboxService.buildTransactionFinalizedEvent(
                        null
                )
        )
                .isInstanceOf(NullPointerException.class);
    }

    /*
     =========================================================
     VERIFY PAYLOAD SERIALIZATION
     =========================================================
     */
    @Test
    void shouldSerializeTransactionFinalizedEventPayload()
            throws Exception {

        when(objectMapper.writeValueAsString(any(
                TransactionFinalizedEvent.class
        ))).thenReturn("{json}");

        OutboxEvent result =
                outboxService.buildTransactionFinalizedEvent(
                        transaction
                );

        assertThat(result.getPayload())
                .isEqualTo("{json}");

        verify(objectMapper).writeValueAsString(any(
                TransactionFinalizedEvent.class
        ));
    }

    /*
     =========================================================
     VERIFY EVENT METADATA
     =========================================================
     */
    @Test
    void shouldPopulateCorrectEventMetadata()
            throws Exception {

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{payload}");

        OutboxEvent result =
                outboxService.buildTransactionFinalizedEvent(
                        transaction
                );

        assertThat(result.getAggregateType())
                .isEqualTo("TRANSACTION");

        assertThat(result.getEventType())
                .isEqualTo("TRANSACTION_FINALIZED");

        assertThat(result.getEventVersion())
                .isEqualTo(1);

        assertThat(result.getAggregateId())
                .isEqualTo(transaction.getTransactionId());
    }
}