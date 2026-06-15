package com.gringotts.risk.risk_decision_service.api.controller;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.risk.risk_decision_service.AdminDto.RiskDecisionAdminResponse;
import com.gringotts.risk.risk_decision_service.application.RiskDecisionApplicationService;
import com.gringotts.risk.risk_decision_service.infrastructure.aiService.AiInvestigationService;
import com.gringotts.risk.risk_decision_service.infrastructure.aiService.InvestigationSummaryResponse;
import jakarta.transaction.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/risk")
@Validated// @Validated at the class level enables constraint checking on // individual method parameters like 'page' and 'size' limits.
public class RiskEvaluationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskEvaluationController.class);

    private final RiskDecisionApplicationService service;
    private final AiInvestigationService aiInvestigationService;
    public RiskEvaluationController(RiskDecisionApplicationService service, AiInvestigationService aiInvestigationService) {
        this.service = service;
        this.aiInvestigationService = aiInvestigationService;
    }

    /**
     * SYSTEM API
     * Called by Transaction Service
     */
    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('RISKCALLER')")
    public ResponseEntity<RiskDecisionResponse> evaluateRisk(
            @Valid @RequestBody RiskDecisionRequest request) {

        LOGGER.info("Risk evaluation request received. transactionId={}", request.getTransactionId());

        RiskDecisionResponse response = service.evaluate(request);

        LOGGER.info("Risk evaluation completed. transactionId={}",
                request.getTransactionId());

        return ResponseEntity.ok(response);
    }

    /**
     * ADMIN API
     * Fetch by Decision ID (Primary Key)
     */
    @GetMapping("/api/v1/decisions/{decisionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskDecisionAdminResponse> getById(
            @PathVariable @NotNull Long decisionId) {

        LOGGER.info("Fetching risk decision by decisionId={}", decisionId);

        RiskDecisionAdminResponse response = service.getById(decisionId);

        return ResponseEntity.ok(response);
    }

    /**
     * ADMIN API
     * Fetch by Transaction ID (1:1 mapping)
     */
    @GetMapping("/api/v1/decisions/transaction/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskDecisionAdminResponse> getDecisionByTransactionId(
            @PathVariable @NotBlank String transactionId) {

        LOGGER.info("Fetching risk decision by transactionId={}", transactionId);

        RiskDecisionAdminResponse response = service.getDecisionByTransactionId(transactionId);

        return ResponseEntity.ok(response);
    }

    /**
     * ADMIN API
     * Paginated listing for audit / dashboards
     */
    @GetMapping("/api/v1/decisions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RiskDecisionAdminResponse>> getAllDecisions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {

        LOGGER.info("Fetching risk decisions. page={}, size={}, from={}, to={}",
                page, size, from, to);

        Page<RiskDecisionAdminResponse> response =
                service.getAllDecisions(page, size, from, to);

        return ResponseEntity.ok(response);
    }

    //AI Api
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/ai/{transactionId}/investigation-summary")
    public ResponseEntity<InvestigationSummaryResponse> investigate(
            @PathVariable String transactionId) {
        InvestigationSummaryResponse response = aiInvestigationService
                .generateInvestigationSummary(transactionId);
        return ResponseEntity.ok(response);
    }
}
