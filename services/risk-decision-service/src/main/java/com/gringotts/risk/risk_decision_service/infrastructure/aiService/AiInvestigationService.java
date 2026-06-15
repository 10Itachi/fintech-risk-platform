package com.gringotts.risk.risk_decision_service.infrastructure.aiService;

import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import com.gringotts.risk.risk_decision_service.exception.RiskDecisionNotFoundException;
import com.gringotts.risk.risk_decision_service.infrastructure.repository.RiskDecisionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AiInvestigationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AiInvestigationService.class);

    private final ChatClient chatClient;
    private final RiskDecisionRepository repository;
    private final FraudPromptBuilder promptBuilder;
    private final MeterRegistry meterRegistry;

    public AiInvestigationService(
            ChatClient.Builder chatClientBuilder,
            RiskDecisionRepository repository,
            FraudPromptBuilder promptBuilder,
            MeterRegistry meterRegistry) {

        this.chatClient = chatClientBuilder.build();
        this.repository = repository;
        this.promptBuilder = promptBuilder;
        this.meterRegistry = meterRegistry;
    }

    @Cacheable(
            value = "investigationSummaryByTransactionId",
            key = "#transactionId"
    )
    public InvestigationSummaryResponse generateInvestigationSummary(
            String transactionId) {

        meterRegistry.counter(
                "risk.ai.investigation.requests"
        ).increment();

        Timer.Sample timer =
                Timer.start(meterRegistry);

        LOGGER.info(
                "ai_investigation_started transactionId={}",
                transactionId
        );

        try {

            RiskDecisionTraceEntity entity =
                    repository.findByTransactionId(transactionId)
                            .orElseThrow(() ->
                                    new RiskDecisionNotFoundException(
                                            "Risk decision not found for transactionId: "
                                                    + transactionId
                                    ));

            LOGGER.info(
                    "ai_investigation_entity_found transactionId={} decisionId={}",
                    transactionId,
                    entity.getId()
            );

            String prompt =
                    promptBuilder.buildPrompt(entity);

            LOGGER.debug(
                    "ai_prompt_generated transactionId={}",
                    transactionId
            );

            String summary =
                    chatClient.prompt()
                            .user(prompt)
                            .call()
                            .content();

            meterRegistry.counter(
                    "risk.ai.investigation.success"
            ).increment();

            LOGGER.info(
                    "ai_investigation_completed transactionId={}",
                    transactionId
            );

            return new InvestigationSummaryResponse(
                    entity.getTransactionId(),
                    entity.getFinalStatus().name(),
                    summary
            );

        } catch (Exception ex) {

            meterRegistry.counter(
                    "risk.ai.investigation.failure"
            ).increment();

            LOGGER.error(
                    "ai_investigation_failed transactionId={}",
                    transactionId,
                    ex
            );

            throw ex;

        } finally {

            timer.stop(
                    Timer.builder(
                                    "risk.ai.investigation.latency"
                            )
                            .tag("service", "ollama")
                            .register(meterRegistry)
            );
        }
    }
}