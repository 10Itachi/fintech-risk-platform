package com.gringotts.transaction.transaction_service.api.controller;

import com.gringotts.enums.TransactionStatus;
import com.gringotts.transaction.transaction_service.application.service.TransactionCommandService;
import com.gringotts.transaction.transaction_service.application.service.TransactionOrchestratorService;
import com.gringotts.transaction.transaction_service.application.service.TransactionQueryService;
import com.gringotts.transaction.transaction_service.domain.model.RiskDecision;
import com.gringotts.transaction.transaction_service.dto.request.TransactionRequestDto;
import com.gringotts.transaction.transaction_service.dto.response.TransactionResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

   private final TransactionOrchestratorService orchestratorService;
   private final TransactionCommandService commandService;
   private final TransactionQueryService queryService;

    public TransactionController(TransactionOrchestratorService orchestratorService, TransactionCommandService commandService, TransactionQueryService queryService) {
        this.orchestratorService = orchestratorService;
        this.commandService = commandService;
        this.queryService = queryService;
    }
    // =========================
    // CREATE TRANSACTION (USER)
    // =========================

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/user/create")
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {

        log.info("Create transaction request received | userRequest | amount={} | type={} | channel={}",
                request.getAmount(), request.getTransactionType(), request.getChannel());

        TransactionResponseDto response =
                orchestratorService.createTransaction(request, idempotencyKey);

        log.info("Transaction created successfully | transactionId={} | status={}",
                response.getTransactionId(), response.getTransactionStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================
    // GET TRANSACTION BY ID (USER + ADMIN)
    // =========================

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/shared/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(
            @PathVariable UUID transactionId
    ) {

        log.info("Fetch transaction by id | transactionId={}", transactionId);

        TransactionResponseDto response =
                queryService.getById(transactionId);

        log.info("Transaction fetched | transactionId={} | status={}",
                response.getTransactionId(), response.getTransactionStatus());

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET MY TRANSACTIONS (USER)
    // =========================

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/my")
    public ResponseEntity<Page<TransactionResponseDto>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {

        log.info("Fetch my transactions | page={} | size={} | sort={}", page, size, sort);

        Page<TransactionResponseDto> response =
                queryService.getMyTransactions(page, size, sort);

        log.info("Fetched {} transactions for current user", response.getNumberOfElements());

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET TRANSACTIONS BY USER (ADMIN)
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userId}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {

        log.info("Admin fetch transactions by userId={} | page={} | size={}", userId, page, size);

        Page<TransactionResponseDto> response =
                queryService.getAllTransactionsByUserId(userId, page, size, sort);

        log.info("Admin fetched {} transactions for userId={}", response.getNumberOfElements(), userId);

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET ALL TRANSACTIONS (ADMIN)
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/allUsers")
    public ResponseEntity<Page<TransactionResponseDto>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {

        log.info("Admin fetch all transactions | page={} | size={} | sort={}", page, size, sort);

        Page<TransactionResponseDto> response =
                queryService.getAllTransactions(page, size, sort);

        log.info("Admin fetched {} transactions", response.getNumberOfElements());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/riskdecision/{transactionId}")
    public ResponseEntity<RiskDecision> getRiskDecisionByTransId(@PathVariable UUID transactionId) {
        log.info("Fetch transaction by id | transactionId={}", transactionId);
        RiskDecision riskDecision= queryService.getRiskDecisionByTransId(transactionId);
        return ResponseEntity.ok(riskDecision);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{transactionId}/review/approve")
    public ResponseEntity<TransactionResponseDto> approveTransaction(
            @PathVariable UUID transactionId
    ) {
        log.info("Admin approving transaction | txnId={}", transactionId);
        TransactionResponseDto response =
                commandService.processManualReview(transactionId, TransactionStatus.APPROVED);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{transactionId}/review/reject")
    public ResponseEntity<TransactionResponseDto> rejectTransaction(
            @PathVariable UUID transactionId
    ) {
        log.info("Admin rejecting transaction | txnId={}", transactionId);
        TransactionResponseDto response =
                commandService.processManualReview(transactionId, TransactionStatus.DECLINED);
        return ResponseEntity.ok(response);
    }
}