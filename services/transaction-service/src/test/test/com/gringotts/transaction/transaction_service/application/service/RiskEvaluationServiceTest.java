package test.com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.transaction.transaction_service.application.service.RiskEvaluationService;
import com.gringotts.transaction.transaction_service.infrastructure.feignclient.RiskServiceClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RiskEvaluationServiceTest {

    @Mock
    private RiskServiceClient riskServiceClient;

    private RiskEvaluationService service;

    private RiskDecisionRequest request;

    @BeforeEach
    void setUp() {

        service = new RiskEvaluationService(
                riskServiceClient,
                new SimpleMeterRegistry()
        );

        request = new RiskDecisionRequest();

        request.setTransactionId(UUID.randomUUID());
    }

    /*
     =========================================================
     SUCCESS FLOW
     =========================================================
     */
    @Test
    void shouldCallRiskServiceSuccessfully() {

        RiskDecisionResponse response =
                new RiskDecisionResponse();

        response.setTransactionStatus(
                TransactionStatus.APPROVED
        );

        when(riskServiceClient.evaluateRisk(request))
                .thenReturn(response);

        RiskDecisionResponse result =
                service.callRiskService(request);

        assertThat(result).isNotNull();

        assertThat(result.getTransactionStatus())
                .isEqualTo(TransactionStatus.APPROVED);

        verify(riskServiceClient, times(1))
                .evaluateRisk(request);
    }

    /*
     =========================================================
     CLIENT FAILURE
     =========================================================
     */
    @Test
    void shouldThrowExceptionWhenRiskServiceFails() {

        when(riskServiceClient.evaluateRisk(request))
                .thenThrow(
                        new RuntimeException("Risk service unavailable")
                );

        assertThatThrownBy(() ->
                service.callRiskService(request)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Risk service unavailable");

        verify(riskServiceClient, times(1))
                .evaluateRisk(request);
    }

    /*
     =========================================================
     FALLBACK RESPONSE
     =========================================================
     */
    @Test
    void shouldReturnReviewFallbackResponse() {

        RuntimeException ex =
                new RuntimeException("Circuit breaker open");

        RiskDecisionResponse response =
                service.riskFallback(
                        request,
                        ex
                );

        assertThat(response).isNotNull();

        assertThat(response.getTransactionStatus())
                .isEqualTo(TransactionStatus.REVIEW);
    }

    /*
     =========================================================
     FALLBACK SHOULD HANDLE NULL EXCEPTION
     =========================================================
     */
    @Test
    void shouldHandleNullExceptionInFallback() {

        RiskDecisionResponse response =
                service.riskFallback(
                        request,
                        null
                );

        assertThat(response).isNotNull();

        assertThat(response.getTransactionStatus())
                .isEqualTo(TransactionStatus.REVIEW);
    }

    /*
     =========================================================
     VERIFY CLIENT INVOCATION ONLY ONCE
     =========================================================
     */
    @Test
    void shouldInvokeRiskClientExactlyOnce() {

        RiskDecisionResponse response =
                new RiskDecisionResponse();

        response.setTransactionStatus(
                TransactionStatus.DECLINED
        );

        when(riskServiceClient.evaluateRisk(any()))
                .thenReturn(response);

        service.callRiskService(request);

        verify(riskServiceClient, times(1))
                .evaluateRisk(any());
    }
}