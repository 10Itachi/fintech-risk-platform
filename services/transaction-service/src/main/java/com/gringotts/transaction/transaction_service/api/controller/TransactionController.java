package com.gringotts.transaction.transaction_service.api.controller;

import com.gringotts.transaction.transaction_service.api.dto.request.TransactionRequestDto;
import com.gringotts.transaction.transaction_service.api.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.application.service.TransactionCommandService;
import com.gringotts.transaction.transaction_service.application.service.TransactionQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionCommandService commandService;
    private final TransactionQueryService queryService;

    public TransactionController(TransactionCommandService commandService, TransactionQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/createTransaction")
    public ResponseEntity<TransactionResponseDto> createTransaction(@RequestBody TransactionRequestDto transactionRequestDto) {
        TransactionResponseDto transaction= commandService.createTransaction(transactionRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/getTransactionById/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable ("transactionId") UUID transactionId) {
        return ResponseEntity.status(HttpStatus.OK).body((TransactionResponseDto) queryService.getById(transactionId));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/getAllTransactionsByUserId")
    public ResponseEntity<Page<TransactionResponseDto>> getAllTransactionsByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort)  {
        Page<TransactionResponseDto> list = queryService.getAllTransactionsByUserId(userId, page, size, sort);
        return ResponseEntity.status(HttpStatus.OK).body(list);
     }

}
