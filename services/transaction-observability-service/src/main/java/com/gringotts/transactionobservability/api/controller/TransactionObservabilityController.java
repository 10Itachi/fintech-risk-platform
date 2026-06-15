package com.gringotts.transactionobservability.api.controller;

import com.gringotts.transactionobservability.domain.model.FailedTransactionEvent;
import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;
import com.gringotts.transactionobservability.service.ExcelService;
import com.gringotts.transactionobservability.service.TransactionObservabilityQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/observability")
@RequiredArgsConstructor
public class TransactionObservabilityController {

    private final TransactionObservabilityQueryService transactionObservabilityQueryService;
    private final ExcelService excelService;
    @GetMapping("/transactions/{txnId}")
    public ResponseEntity<TransactionEventRecord> getTransaction(@PathVariable UUID txnId) {
        return ResponseEntity.ok(transactionObservabilityQueryService.getByTransactionId(txnId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionEventRecord>> getTransactions(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20, sort = "recordedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionObservabilityQueryService.getAll(from, to, pageable)
        );
    }

    @GetMapping("/failed-events")
    public ResponseEntity<Page<FailedTransactionEvent>> getFailedEvents(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionObservabilityQueryService.getFailedEvents(from, to, pageable)
        );
    }

    @GetMapping("/failed-events/{id}")
    public ResponseEntity<FailedTransactionEvent> getFailedEvent(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionObservabilityQueryService.getFailedEvent(id));
    }

    @GetMapping("/transactions/export")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        List<TransactionEventRecord> data = transactionObservabilityQueryService.exportTransactions(from, to);

        byte[] excel = excelService.generateTransactionExcel(data);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=ledger.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excel);
    }
}
