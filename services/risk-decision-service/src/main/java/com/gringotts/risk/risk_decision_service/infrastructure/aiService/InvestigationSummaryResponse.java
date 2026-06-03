package com.gringotts.risk.risk_decision_service.infrastructure.aiService;

public record InvestigationSummaryResponse(
        String transactionId,
        String status,
        String summary
) {
}
