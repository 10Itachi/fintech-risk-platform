package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.transaction.transaction_service.api.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.api.exception.TransactionNotFound;
import com.gringotts.transaction.transaction_service.application.mapper.TransactionMapper;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionQueryService(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    public TransactionResponseDto getById(UUID transactionId) {
        return transactionMapper.toDto(transactionRepository
                .findById(transactionId)
                .orElseThrow(()->new TransactionNotFound("Transaction not found with id " + transactionId)));
    }

    public Page<TransactionResponseDto> getAllTransactionsByUserId(Long userId, int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String sortField = "createdAt";

        if (sort != null && sort.contains(",")) {
            String[] sortParam = sort.split(",");
            sortField = sortParam[0];
            direction = sortParam[1].equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Transaction> transaction = transactionRepository.findByUserId(userId,pageable);
        return transaction.map(transactionMapper::toDto);
    }
}
