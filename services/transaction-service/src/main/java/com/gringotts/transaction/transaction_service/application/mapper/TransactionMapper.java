package com.gringotts.transaction.transaction_service.application.mapper;

import com.gringotts.transaction.transaction_service.api.dto.request.TransactionRequestDto;
import com.gringotts.transaction.transaction_service.api.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "transactionStatus", ignore = true)

    @Mapping(target = "totalAmountLast24h", ignore = true)
    @Mapping(target = "txnCountLast24h", ignore = true)
    Transaction toEntity(TransactionRequestDto transactionRequestDto);

    TransactionResponseDto toDto(Transaction transaction);


}
