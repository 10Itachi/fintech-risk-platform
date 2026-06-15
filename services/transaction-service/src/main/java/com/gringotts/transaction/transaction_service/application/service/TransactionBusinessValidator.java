package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionType;
import com.gringotts.transaction.transaction_service.domain.exception.InvalidTransactionException;
import com.gringotts.transaction.transaction_service.dto.request.TransactionRequestDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionBusinessValidator {

    public void validate(TransactionRequestDto request) {
        // 1. Basic Presence & Amount (Rule 1, 2, 6)
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than 0");
        }
        if (request.getTransactionType() == null) {
            throw new InvalidTransactionException("Transaction type must be inputed");
        }
        if (request.getChannel() == null) {
            throw new InvalidTransactionException("Channel must be present");
        }

        // 2. Transaction Type Specific Rules (Rule 3, 4, 5)
        validateAccountRequirements(request);

        // 3. Channel Restrictions (Rule 7, 8, 9)
        validateChannelRestrictions(request);
    }

    private void validateAccountRequirements(TransactionRequestDto request) {
        TransactionType type = request.getTransactionType();
        String source = request.getSourceAccount();
        String target = request.getTargetAccount();

        switch (type) {
            case WITHDRAWAL -> {
                if (isEmpty(source) || !isEmpty(target)) {
                    throw new InvalidTransactionException("Withdrawal requires source account only; target must be empty");
                }
            }
            case DEPOSIT -> {
                if (isEmpty(source) || !isEmpty(target)) {
                    throw new InvalidTransactionException("Deposit requires source account only; target must be empty");
                }
            }
            case TRANSFER -> {
                if (isEmpty(source) || isEmpty(target)) {
                    throw new InvalidTransactionException("Transfer requires both source and target accounts");
                }
                if (source.equals(target)) {
                    throw new InvalidTransactionException("Source and target accounts must be different for transfers");
                }
            }
        }
    }

    private void validateChannelRestrictions(TransactionRequestDto request) {
        Channel channel = request.getChannel();
        TransactionType type = request.getTransactionType();

        // Rule 7: UPI restrictions
        if (channel == Channel.UPI && (type == TransactionType.WITHDRAWAL || type == TransactionType.DEPOSIT)) {
            throw new InvalidTransactionException("UPI cannot be used for Withdrawal or Deposit; only for Transfers");
        }

        // Rule 8: CARD restrictions
        if (channel == Channel.CARD && type != TransactionType.WITHDRAWAL) {
            throw new InvalidTransactionException("CARD can only be used for Withdrawal transactions");
        }

        // Rule 9: NET_BANKING (Implicitly allowed for all since no restrictions are added)
        // If we strictly want to allow only these, we could add a default rejection here.
    }

    private boolean isEmpty(String str) {
        return str == null || str.isBlank();
    }
}