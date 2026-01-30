package com.gringotts.transaction.transaction_service.infrastructure.client;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "risk-decision-service",
        url ="${risk.service.url}"
)
public interface RiskServiceClient {

    @PostMapping("/risk/evaluate")
    RiskDecisionResponse evaluateRisk(@RequestBody RiskDecisionRequest riskDecisionRequest);
}
