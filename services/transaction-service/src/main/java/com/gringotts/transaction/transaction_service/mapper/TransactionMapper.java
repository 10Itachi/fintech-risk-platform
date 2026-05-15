package com.gringotts.transaction.transaction_service.mapper;

import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.dto.request.TransactionRequestDto;
import com.gringotts.transaction.transaction_service.dto.response.TransactionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    // REQUEST DTO → ENTITY
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "transactionStatus", ignore = true)
    @Mapping(target = "internalStatus", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)

    @Mapping(target = "idempotencyKey", ignore = true)

    // derived / system fields
    @Mapping(target = "totalAmountLast24h", ignore = true)
    @Mapping(target = "txnCountLast24h", ignore = true)

    Transaction toEntity(TransactionRequestDto request);

    // ENTITY → RESPONSE DTO
    TransactionResponseDto toDto(Transaction transaction);
}