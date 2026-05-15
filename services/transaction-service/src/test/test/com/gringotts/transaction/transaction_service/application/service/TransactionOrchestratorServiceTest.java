package test.com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.enums.TransactionType;
import com.gringotts.transaction.transaction_service.application.service.*;
import com.gringotts.transaction.transaction_service.domain.businessEnums.InternalTransactionStatus;
import com.gringotts.transaction.transaction_service.domain.exception.TransactionInProgressException;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.dto.request.TransactionRequestDto;
import com.gringotts.transaction.transaction_service.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.infrastructure.redis.RedisIdempotencyService;
import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import com.gringotts.transaction.transaction_service.mapper.RiskRequestMapper;
import com.gringotts.transaction.transaction_service.mapper.TransactionMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.gringotts.transaction.transaction_service.config.security.SecurityUtils;
import com.gringotts.transaction.transaction_service.Utils.RequestMetadataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TransactionOrchestratorServiceTest {

    @Mock
    private TransactionCommandService commandService;

    @Mock
    private RiskEvaluationService riskService;

    @Mock
    private RiskRequestMapper riskRequestMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionRiskFeatureService riskFeatureService;

    @Mock
    private TransactionBusinessValidator businessValidator;

    @Mock
    private RedisIdempotencyService redisIdempotencyService;

    private TransactionOrchestratorService service;

    private TransactionRequestDto request;

    private final String IDEMPOTENCY_KEY = "idem-123";

    @BeforeEach
    void setUp() {

        service = new TransactionOrchestratorService(
                commandService,
                riskService,
                riskRequestMapper,
                transactionRepository,
                transactionMapper,
                riskFeatureService,
                businessValidator,
                new SimpleMeterRegistry(),
                redisIdempotencyService
        );

        request = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(1000))
                .transactionType(TransactionType.TRANSFER)
                .channel(Channel.UPI)
                .sourceAccount("ACC-IN-123456")
                .targetAccount("ACC-IN-654321")
                .transactionTime(Instant.now())
                .build();
    }

    /*
     =========================================================
     SUCCESS FLOW
     =========================================================
     */
    @Test
    void shouldCreateTransactionSuccessfully() {

        UUID txnId = UUID.randomUUID();

        Transaction txn = new Transaction();
        txn.setTransactionId(txnId);
        txn.setUserId(UUID.randomUUID());
        txn.setCreatedAt(Instant.now());

        RiskDecisionRequest riskRequest =
                new RiskDecisionRequest();

        RiskDecisionResponse riskResponse =
                new RiskDecisionResponse();

        TransactionResponseDto response =
                TransactionResponseDto.builder()
                        .transactionId(txnId)
                        .transactionStatus(TransactionStatus.APPROVED)
                        .build();

        try (
                MockedStatic<SecurityUtils> securityMock =
                        mockStatic(SecurityUtils.class);

                MockedStatic<RequestMetadataUtils> requestMock =
                        mockStatic(RequestMetadataUtils.class)
        ) {

            securityMock.when(SecurityUtils::getUserId)
                    .thenReturn(UUID.randomUUID());

            securityMock.when(SecurityUtils::getUsername)
                    .thenReturn("sangmesh");

            securityMock.when(SecurityUtils::getEmail)
                    .thenReturn("test@gmail.com");

            requestMock.when(() ->
                            RequestMetadataUtils.getDeviceId(any()))
                    .thenReturn("DEV-ANDROID-12345");

            requestMock.when(RequestMetadataUtils::getCountry)
                    .thenReturn("IN");

            when(redisIdempotencyService.tryLock(IDEMPOTENCY_KEY))
                    .thenReturn(true);

            when(commandService.createInitialTransaction(any()))
                    .thenReturn(txn);

            when(riskFeatureService.totalAmountLast24H(any(), any()))
                    .thenReturn(BigDecimal.valueOf(5000));

            when(riskFeatureService.numberOfTransactionsLast24h(any(), any()))
                    .thenReturn(5);

            when(riskRequestMapper.toRiskRequest(any()))
                    .thenReturn(riskRequest);

            when(riskService.callRiskService(any()))
                    .thenReturn(riskResponse);

            when(commandService.finalizeTransaction(any(), any(), anyLong()))
                    .thenReturn(response);

            TransactionResponseDto result =
                    service.createTransaction(
                            request,
                            IDEMPOTENCY_KEY
                    );

            assertThat(result).isNotNull();

            assertThat(result.getTransactionStatus())
                    .isEqualTo(TransactionStatus.APPROVED);

            verify(commandService, times(1))
                    .createInitialTransaction(any());

            verify(commandService, times(1))
                    .finalizeTransaction(any(), any(), anyLong());

            verify(redisIdempotencyService, times(1))
                    .markCompleted(anyString(), anyString());
        }
    }

    /*
     =========================================================
     DUPLICATE REQUEST FROM REDIS
     =========================================================
     */
    @Test
    void shouldReturnExistingTransactionForDuplicateRequest() {

        UUID txnId = UUID.randomUUID();

        Transaction txn = new Transaction();
        txn.setTransactionId(txnId);
        txn.setInternalStatus(
                InternalTransactionStatus.RISK_APPROVED
        );

        TransactionResponseDto dto =
                TransactionResponseDto.builder()
                        .transactionId(txnId)
                        .transactionStatus(TransactionStatus.APPROVED)
                        .build();

        when(redisIdempotencyService.tryLock(IDEMPOTENCY_KEY))
                .thenReturn(false);

        when(redisIdempotencyService
                .getCompletedTransactionId(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(txnId.toString()));

        when(transactionRepository.findById(txnId))
                .thenReturn(Optional.of(txn));

        when(transactionMapper.toDto(txn))
                .thenReturn(dto);

        TransactionResponseDto result =
                service.createTransaction(
                        request,
                        IDEMPOTENCY_KEY
                );

        assertThat(result).isNotNull();

        verify(commandService, never())
                .createInitialTransaction(any());
    }

    /*
     =========================================================
     TRANSACTION STILL PROCESSING
     =========================================================
     */
    @Test
    void shouldThrowTransactionInProgressException() {

        when(redisIdempotencyService.tryLock(IDEMPOTENCY_KEY))
                .thenReturn(false);

        when(redisIdempotencyService
                .getCompletedTransactionId(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(transactionRepository
                .findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createTransaction(
                        request,
                        IDEMPOTENCY_KEY
                )
        )
                .isInstanceOf(TransactionInProgressException.class);
    }

    /*
     =========================================================
     REDIS FAILURE SHOULD FALLBACK TO DB
     =========================================================
     */
    @Test
    void shouldContinueWhenRedisFails() {

        try (
                MockedStatic<SecurityUtils> securityMock =
                        mockStatic(SecurityUtils.class);

                MockedStatic<RequestMetadataUtils> requestMock =
                        mockStatic(RequestMetadataUtils.class)
        ) {

            securityMock.when(SecurityUtils::getUserId)
                    .thenReturn(UUID.randomUUID());

            securityMock.when(SecurityUtils::getUsername)
                    .thenReturn("sangmesh");

            securityMock.when(SecurityUtils::getEmail)
                    .thenReturn("test@gmail.com");

            requestMock.when(() ->
                            RequestMetadataUtils.getDeviceId(any()))
                    .thenReturn("DEV-ANDROID-12345");

            requestMock.when(RequestMetadataUtils::getCountry)
                    .thenReturn("IN");

            when(redisIdempotencyService.tryLock(anyString()))
                    .thenThrow(
                            new RuntimeException("Redis down")
                    );

            Transaction txn = new Transaction();
            txn.setTransactionId(UUID.randomUUID());
            txn.setCreatedAt(Instant.now());

            when(commandService.createInitialTransaction(any()))
                    .thenReturn(txn);

            when(riskFeatureService.totalAmountLast24H(any(), any()))
                    .thenReturn(BigDecimal.TEN);

            when(riskFeatureService.numberOfTransactionsLast24h(any(), any()))
                    .thenReturn(1);

            when(riskRequestMapper.toRiskRequest(any()))
                    .thenReturn(new RiskDecisionRequest());

            when(riskService.callRiskService(any()))
                    .thenReturn(new RiskDecisionResponse());

            when(commandService.finalizeTransaction(any(), any(), anyLong()))
                    .thenReturn(
                            TransactionResponseDto.builder()
                                    .transactionStatus(TransactionStatus.APPROVED)
                                    .build()
                    );

            assertThatCode(() ->
                    service.createTransaction(
                            request,
                            IDEMPOTENCY_KEY
                    )
            ).doesNotThrowAnyException();
        }
    }

    /*
     =========================================================
     FAILURE SHOULD RELEASE REDIS LOCK
     =========================================================
     */
    @Test
    void shouldReleaseRedisLockWhenFailureOccurs() {

        doThrow(new RuntimeException("validation failed"))
                .when(businessValidator)
                .validate(any());

        when(redisIdempotencyService.tryLock(anyString()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.createTransaction(
                        request,
                        IDEMPOTENCY_KEY
                )
        )
                .isInstanceOf(RuntimeException.class);

        verify(redisIdempotencyService, times(1))
                .releaseLock(IDEMPOTENCY_KEY);
    }
}