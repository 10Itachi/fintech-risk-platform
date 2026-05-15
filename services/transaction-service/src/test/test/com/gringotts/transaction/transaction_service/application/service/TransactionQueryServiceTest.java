package test.com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.transaction.transaction_service.application.service.TransactionQueryService;
import com.gringotts.transaction.transaction_service.config.security.SecurityUtils;
import com.gringotts.transaction.transaction_service.domain.exception.RiskDecisionNotFoundException;
import com.gringotts.transaction.transaction_service.domain.exception.TransactionNotFoundException;
import com.gringotts.transaction.transaction_service.domain.model.RiskDecision;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.infrastructure.repository.RiskDecisionRepository;
import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import com.gringotts.transaction.transaction_service.mapper.TransactionMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TransactionQueryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private RiskDecisionRepository riskDecisionRepository;

    private TransactionQueryService service;

    private UUID userId;

    private UUID transactionId;

    private Transaction transaction;

    private TransactionResponseDto responseDto;

    @BeforeEach
    void setUp() {

        service = new TransactionQueryService(
                transactionRepository,
                transactionMapper,
                riskDecisionRepository,
                new SimpleMeterRegistry()
        );

        userId = UUID.randomUUID();

        transactionId = UUID.randomUUID();

        transaction = new Transaction();

        transaction.setTransactionId(transactionId);
        transaction.setUserId(userId);
        transaction.setAmount(BigDecimal.valueOf(1000));
        transaction.setCreatedAt(Instant.now());

        responseDto = TransactionResponseDto.builder()
                .transactionId(transactionId)
                .amount(BigDecimal.valueOf(1000))
                .build();
    }

    /*
     =========================================================
     GET TRANSACTION BY ID SUCCESS
     =========================================================
     */
    @Test
    void shouldGetTransactionByIdSuccessfully() {

        try (MockedStatic<SecurityUtils> securityMock =
                     mockStatic(SecurityUtils.class)) {

            securityMock.when(SecurityUtils::getUserId)
                    .thenReturn(userId);

            when(transactionRepository.findByTransactionIdAndUserId(
                    transactionId,
                    userId
            )).thenReturn(Optional.of(transaction));

            when(transactionMapper.toDto(transaction))
                    .thenReturn(responseDto);

            TransactionResponseDto result =
                    service.getById(transactionId);

            assertThat(result).isNotNull();

            assertThat(result.getTransactionId())
                    .isEqualTo(transactionId);

            verify(transactionRepository, times(1))
                    .findByTransactionIdAndUserId(
                            transactionId,
                            userId
                    );
        }
    }

    /*
     =========================================================
     TRANSACTION NOT FOUND
     =========================================================
     */
    @Test
    void shouldThrowWhenTransactionNotFound() {

        try (MockedStatic<SecurityUtils> securityMock =
                     mockStatic(SecurityUtils.class)) {

            securityMock.when(SecurityUtils::getUserId)
                    .thenReturn(userId);

            when(transactionRepository.findByTransactionIdAndUserId(
                    transactionId,
                    userId
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.getById(transactionId)
            )
                    .isInstanceOf(TransactionNotFoundException.class);
        }
    }

    /*
     =========================================================
     NULL TRANSACTION ID
     =========================================================
     */
    @Test
    void shouldThrowWhenTransactionIdIsNull() {

        assertThatThrownBy(() ->
                service.getById(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TransactionId cannot be null");
    }

    /*
     =========================================================
     GET MY TRANSACTIONS
     =========================================================
     */
    @Test
    void shouldGetMyTransactionsSuccessfully() {

        try (MockedStatic<SecurityUtils> securityMock =
                     mockStatic(SecurityUtils.class)) {

            securityMock.when(SecurityUtils::getUserId)
                    .thenReturn(userId);

            Page<Transaction> page =
                    new PageImpl<>(
                            java.util.List.of(transaction)
                    );

            when(transactionRepository.findByUserId(
                    eq(userId),
                    any(Pageable.class)
            )).thenReturn(page);

            when(transactionMapper.toDto(any()))
                    .thenReturn(responseDto);

            Page<TransactionResponseDto> result =
                    service.getMyTransactions(
                            0,
                            10,
                            "createdAt,desc"
                    );

            assertThat(result.getContent())
                    .hasSize(1);

            verify(transactionRepository, times(1))
                    .findByUserId(
                            eq(userId),
                            any(Pageable.class)
                    );
        }
    }

    /*
     =========================================================
     GET ALL TRANSACTIONS
     =========================================================
     */
    @Test
    void shouldGetAllTransactionsSuccessfully() {

        Page<Transaction> page =
                new PageImpl<>(
                        java.util.List.of(transaction)
                );

        when(transactionRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        when(transactionMapper.toDto(any()))
                .thenReturn(responseDto);

        Page<TransactionResponseDto> result =
                service.getAllTransactions(
                        0,
                        20,
                        "amount,asc"
                );

        assertThat(result.getContent())
                .hasSize(1);

        verify(transactionRepository, times(1))
                .findAll(any(Pageable.class));
    }

    /*
     =========================================================
     GET USER TRANSACTIONS BY ADMIN
     =========================================================
     */
    @Test
    void shouldGetTransactionsByUserIdSuccessfully() {

        Page<Transaction> page =
                new PageImpl<>(
                        java.util.List.of(transaction)
                );

        when(transactionRepository.findByUserId(
                eq(userId),
                any(Pageable.class)
        )).thenReturn(page);

        when(transactionMapper.toDto(any()))
                .thenReturn(responseDto);

        Page<TransactionResponseDto> result =
                service.getAllTransactionsByUserId(
                        userId,
                        0,
                        10,
                        "createdAt,desc"
                );

        assertThat(result.getContent())
                .hasSize(1);

        verify(transactionRepository, times(1))
                .findByUserId(
                        eq(userId),
                        any(Pageable.class)
                );
    }

    /*
     =========================================================
     GET RISK DECISION SUCCESS
     =========================================================
     */
    @Test
    void shouldGetRiskDecisionSuccessfully() {

        RiskDecision decision =
                new RiskDecision();

        decision.setRiskScore(85);

        when(riskDecisionRepository.findByTransactionId(
                transactionId
        )).thenReturn(Optional.of(decision));

        RiskDecision result =
                service.getRiskDecisionByTransId(
                        transactionId
                );

        assertThat(result).isNotNull();

        assertThat(result.getRiskScore())
                .isEqualTo(85);

        verify(riskDecisionRepository, times(1))
                .findByTransactionId(transactionId);
    }

    /*
     =========================================================
     RISK DECISION NOT FOUND
     =========================================================
     */
    @Test
    void shouldThrowWhenRiskDecisionNotFound() {

        when(riskDecisionRepository.findByTransactionId(
                transactionId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getRiskDecisionByTransId(
                        transactionId
                )
        )
                .isInstanceOf(RiskDecisionNotFoundException.class);
    }

    /*
     =========================================================
     INVALID SORT FIELD SHOULD FALLBACK
     =========================================================
     */
    @Test
    void shouldFallbackForInvalidSortField() {

        Page<Transaction> page =
                new PageImpl<>(
                        java.util.List.of(transaction)
                );

        when(transactionRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        when(transactionMapper.toDto(any()))
                .thenReturn(responseDto);

        Page<TransactionResponseDto> result =
                service.getAllTransactions(
                        0,
                        10,
                        "hackField,asc"
                );

        assertThat(result).isNotNull();

        verify(transactionRepository, times(1))
                .findAll(any(Pageable.class));
    }

    /*
     =========================================================
     PAGE SIZE SHOULD CAP AT 100
     =========================================================
     */
    @Test
    void shouldCapPageSizeAt100() {

        Page<Transaction> page =
                new PageImpl<>(
                        java.util.List.of(transaction)
                );

        when(transactionRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        when(transactionMapper.toDto(any()))
                .thenReturn(responseDto);

        service.getAllTransactions(
                0,
                500,
                "createdAt,desc"
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(transactionRepository)
                .findAll(captor.capture());

        Pageable pageable =
                captor.getValue();

        assertThat(pageable.getPageSize())
                .isEqualTo(100);
    }
}