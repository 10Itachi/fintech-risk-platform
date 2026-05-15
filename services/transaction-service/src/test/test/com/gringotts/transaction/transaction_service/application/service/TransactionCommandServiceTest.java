package test.com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.transaction.transaction_service.application.service.OutboxService;
import com.gringotts.transaction.transaction_service.application.service.TransactionCommandService;
import com.gringotts.transaction.transaction_service.domain.businessEnums.InternalTransactionStatus;
import com.gringotts.transaction.transaction_service.domain.exception.InvalidTransactionStateException;
import com.gringotts.transaction.transaction_service.domain.model.OutboxEvent;
import com.gringotts.transaction.transaction_service.domain.model.RiskDecision;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.infrastructure.repository.OutboxEventRepository;
import com.gringotts.transaction.transaction_service.infrastructure.repository.RiskDecisionRepository;
import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import com.gringotts.transaction.transaction_service.mapper.RiskDecisionMapper;
import com.gringotts.transaction.transaction_service.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TransactionCommandServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private RiskDecisionRepository riskDecisionRepository;

    @Mock
    private RiskDecisionMapper riskDecisionMapper;

    @Mock
    private OutboxService outboxService;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private TransactionCommandService service;

    private Transaction transaction;

    @BeforeEach
    void setUp() {

        transaction = new Transaction();

        transaction.setTransactionId(UUID.randomUUID());

        transaction.setInternalStatus(
                InternalTransactionStatus.INITIATED
        );

        transaction.setTransactionStatus(
                TransactionStatus.INITIATED
        );

        transaction.setCreatedAt(Instant.now());

        transaction.setUpdatedAt(Instant.now());
    }

    /*
     =========================================================
     CREATE INITIAL TRANSACTION
     =========================================================
     */
    @Test
    void shouldCreateInitialTransactionSuccessfully() {

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result =
                service.createInitialTransaction(transaction);

        assertThat(result).isNotNull();

        assertThat(result.getInternalStatus())
                .isEqualTo(InternalTransactionStatus.PENDING_RISK);

        verify(transactionRepository, times(1))
                .save(any(Transaction.class));
    }

    /*
     =========================================================
     FINALIZE APPROVED TRANSACTION
     =========================================================
     */
    @Test
    void shouldFinalizeApprovedTransactionSuccessfully() {

        transaction.setInternalStatus(
                InternalTransactionStatus.PENDING_RISK
        );

        RiskDecisionResponse response =
                new RiskDecisionResponse();

        response.setTransactionStatus(
                TransactionStatus.APPROVED
        );

        RiskDecision riskDecision =
                new RiskDecision();

        OutboxEvent outboxEvent =
                new OutboxEvent();

        TransactionResponseDto dto =
                TransactionResponseDto.builder()
                        .transactionId(transaction.getTransactionId())
                        .transactionStatus(TransactionStatus.APPROVED)
                        .build();

        when(riskDecisionMapper.toEntity(any(), any(), anyLong()))
                .thenReturn(riskDecision);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        when(outboxService.buildTransactionFinalizedEvent(any()))
                .thenReturn(outboxEvent);

        when(transactionMapper.toDto(any()))
                .thenReturn(dto);

        TransactionResponseDto result =
                service.finalizeTransaction(
                        transaction,
                        response,
                        120
                );

        assertThat(result).isNotNull();

        assertThat(result.getTransactionStatus())
                .isEqualTo(TransactionStatus.APPROVED);

        verify(riskDecisionRepository, times(1))
                .save(any(RiskDecision.class));

        verify(outboxEventRepository, times(1))
                .save(any(OutboxEvent.class));
    }

    /*
     =========================================================
     FINALIZE REVIEW TRANSACTION
     =========================================================
     */
    @Test
    void shouldFinalizeReviewTransactionSuccessfully() {

        transaction.setInternalStatus(
                InternalTransactionStatus.PENDING_RISK
        );

        RiskDecisionResponse response =
                new RiskDecisionResponse();

        response.setTransactionStatus(
                TransactionStatus.REVIEW
        );

        when(riskDecisionMapper.toEntity(any(), any(), anyLong()))
                .thenReturn(new RiskDecision());

        when(transactionRepository.save(any()))
                .thenReturn(transaction);

        when(outboxService.buildTransactionFinalizedEvent(any()))
                .thenReturn(new OutboxEvent());

        when(transactionMapper.toDto(any()))
                .thenReturn(
                        TransactionResponseDto.builder()
                                .transactionStatus(TransactionStatus.REVIEW)
                                .build()
                );

        TransactionResponseDto result =
                service.finalizeTransaction(
                        transaction,
                        response,
                        50
                );

        assertThat(result).isNotNull();

        verify(outboxEventRepository, times(1))
                .save(any(OutboxEvent.class));
    }

    /*
     =========================================================
     NULL TRANSACTION
     =========================================================
     */
    @Test
    void shouldThrowWhenTransactionIsNull() {

        RiskDecisionResponse response =
                new RiskDecisionResponse();

        assertThatThrownBy(() ->
                service.finalizeTransaction(
                        null,
                        response,
                        100
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction cannot be null");
    }

    /*
     =========================================================
     NULL RESPONSE
     =========================================================
     */
    @Test
    void shouldThrowWhenRiskResponseIsNull() {

        assertThatThrownBy(() ->
                service.finalizeTransaction(
                        transaction,
                        null,
                        100
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Risk response cannot be null");
    }

    /*
     =========================================================
     INVALID TRANSITION
     =========================================================
     */
    @Test
    void shouldThrowForInvalidStateTransition() {

        transaction.setInternalStatus(
                InternalTransactionStatus.RISK_APPROVED
        );

        RiskDecisionResponse response =
                new RiskDecisionResponse();

        response.setTransactionStatus(
                TransactionStatus.APPROVED
        );

        when(riskDecisionMapper.toEntity(any(), any(), anyLong()))
                .thenReturn(new RiskDecision());

        assertThatThrownBy(() ->
                service.finalizeTransaction(
                        transaction,
                        response,
                        100
                )
        )
                .isInstanceOf(InvalidTransactionStateException.class);
    }

    /*
     =========================================================
     MANUAL REVIEW APPROVED
     =========================================================
     */
    @Test
    void shouldProcessManualReviewApproved() {

        UUID txnId = UUID.randomUUID();

        transaction.setTransactionId(txnId);

        transaction.setInternalStatus(
                InternalTransactionStatus.REVIEW_PENDING
        );

        when(transactionRepository.findById(txnId))
                .thenReturn(Optional.of(transaction));

        when(transactionRepository.save(any()))
                .thenReturn(transaction);

        when(outboxService.buildTransactionFinalizedEvent(any()))
                .thenReturn(new OutboxEvent());

        when(transactionMapper.toDto(any()))
                .thenReturn(
                        TransactionResponseDto.builder()
                                .transactionStatus(TransactionStatus.APPROVED)
                                .build()
                );

        TransactionResponseDto result =
                service.processManualReview(
                        txnId,
                        TransactionStatus.APPROVED
                );

        assertThat(result).isNotNull();

        verify(outboxEventRepository, times(1))
                .save(any(OutboxEvent.class));
    }

    /*
     =========================================================
     MANUAL REVIEW INVALID STATE
     =========================================================
     */
    @Test
    void shouldThrowWhenManualReviewStateInvalid() {

        UUID txnId = UUID.randomUUID();

        transaction.setInternalStatus(
                InternalTransactionStatus.RISK_APPROVED
        );

        when(transactionRepository.findById(txnId))
                .thenReturn(Optional.of(transaction));

        assertThatThrownBy(() ->
                service.processManualReview(
                        txnId,
                        TransactionStatus.APPROVED
                )
        )
                .isInstanceOf(InvalidTransactionStateException.class);
    }

    /*
     =========================================================
     TRANSACTION NOT FOUND
     =========================================================
     */
    @Test
    void shouldThrowWhenTransactionNotFound() {

        UUID txnId = UUID.randomUUID();

        when(transactionRepository.findById(txnId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.processManualReview(
                        txnId,
                        TransactionStatus.APPROVED
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction not found");
    }

    /*
     =========================================================
     DATABASE FAILURE
     =========================================================
     */
    @Test
    void shouldThrowWhenDatabaseFails() {

        when(transactionRepository.save(any()))
                .thenThrow(
                        new DataAccessResourceFailureException(
                                "DB unavailable"
                        )
                );

        assertThatThrownBy(() ->
                service.createInitialTransaction(transaction)
        )
                .isInstanceOf(DataAccessResourceFailureException.class);
    }
}