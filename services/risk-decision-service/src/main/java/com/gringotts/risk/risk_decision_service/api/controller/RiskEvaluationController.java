package com.gringotts.risk.risk_decision_service.api.controller;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.risk.risk_decision_service.application.service.RiskDecisionApplicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risk")
public class RiskEvaluationController {
    private final Logger LOGGER = LoggerFactory.getLogger(RiskEvaluationController.class);
    private final RiskDecisionApplicationService service;

    public RiskEvaluationController(RiskDecisionApplicationService service) {
        this.service = service;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<RiskDecisionResponse> evaluateRisk(@Valid @RequestBody RiskDecisionRequest riskRequest) {
        LOGGER.info("******-inside the risk-decision-service controller");
        RiskDecisionResponse riskResponse = service.evaluate(riskRequest);
        return ResponseEntity.status(HttpStatus.OK).body(riskResponse);
    }

}
