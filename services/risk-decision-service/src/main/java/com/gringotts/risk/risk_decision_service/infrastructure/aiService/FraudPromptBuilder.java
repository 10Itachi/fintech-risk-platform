package com.gringotts.risk.risk_decision_service.infrastructure.aiService;

import com.gringotts.enums.TransactionStatus;
import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import org.springframework.stereotype.Component;

@Component
public class FraudPromptBuilder {

    public String buildPrompt(RiskDecisionTraceEntity entity) {
        if (entity.getFinalStatus() == TransactionStatus.APPROVED) {

            throw new IllegalArgumentException(
                    "Investigation summary is available only for REVIEW and DECLINED transactions"
            );
        }
        String reasons = entity.getReasonCodes() == null
                ? "NONE"
                : String.join(", ", entity.getReasonCodes());

        return """
                You are a senior fraud analyst.

                Analyze the following risk decision.

                Transaction Id:
                %s

                Status:
                %s

                ML Probability:
                %s

                Reason Codes:
                %s

                Policy Version:
                %s

                Return the response as plain text.
                
                Do not use:
                - Markdown
                - **
                - Bullet symbols
                - Numbered lists
                
                Use only simple business language.
                
                Format:
                
                Investigation Summary:
                <summary>
                
                Key Risk Indicators:
                <indicators>
                
                Recommended Action:
                <action>

                Maximum 150 words.
                """
                .formatted(
                        entity.getTransactionId(),
                        entity.getFinalStatus(),
                        entity.getMlProbability(),
                        reasons,
                        entity.getPolicyVersion()
                );
    }
}