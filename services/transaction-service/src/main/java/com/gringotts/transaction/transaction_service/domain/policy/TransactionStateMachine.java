package com.gringotts.transaction.transaction_service.domain.policy;

import com.gringotts.enums.TransactionStatus;
import com.gringotts.transaction.transaction_service.domain.exception.InvalidTransactionStateException;

public final class TransactionStateMachine {

    private TransactionStateMachine() {}

    public static void validateTransition(
            TransactionStatus current,
            TransactionStatus next
    ) {
        if (current == TransactionStatus.INITIATED &&
                (next == TransactionStatus.APPROVED ||
                        next == TransactionStatus.DECLINED ||
                        next == TransactionStatus.REVIEW)) {
            return;
        }

        throw new InvalidTransactionStateException(
                "Invalid transition from " + current + " to " + next
        );
    }
}
