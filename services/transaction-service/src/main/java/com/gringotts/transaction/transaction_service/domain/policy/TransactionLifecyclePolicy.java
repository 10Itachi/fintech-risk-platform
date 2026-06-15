package com.gringotts.transaction.transaction_service.domain.policy;


import com.gringotts.transaction.transaction_service.domain.businessEnums.InternalTransactionStatus;
import com.gringotts.transaction.transaction_service.domain.exception.InvalidTransactionStateException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TransactionLifecyclePolicy {

    private TransactionLifecyclePolicy() {}
    /*
    ============================================================
    INTERNAL STATE TRANSITION RULES (SOURCE OF TRUTH)
    ============================================================
    Valid lifecycle transitions:

    INITIATED        → PENDING_RISK

    PENDING_RISK     → RISK_APPROVED
                     → RISK_REJECTED

    RISK_APPROVED    → LEDGER_PENDING

    LEDGER_PENDING   → LEDGER_SUCCESS
                     → LEDGER_FAILED

    LEDGER_SUCCESS   → COMPLETED

    Terminal states (no transitions allowed):
    RISK_REJECTED
    LEDGER_FAILED
    COMPLETED
    FAILED
    ============================================================
    */

    private static final Map<InternalTransactionStatus, Set<InternalTransactionStatus>> ALLOWED_TRANSITIONS
            = new EnumMap<>(InternalTransactionStatus.class);

    static {

        // Initial state
        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.INITIATED,
                EnumSet.of(InternalTransactionStatus.PENDING_RISK)
        );

        // Waiting for risk decision
        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.PENDING_RISK,
                EnumSet.of(
                        InternalTransactionStatus.RISK_APPROVED,
                        InternalTransactionStatus.RISK_REJECTED,
                        InternalTransactionStatus.REVIEW_PENDING
                )
        );


        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.REVIEW_PENDING,
                EnumSet.of(
                        InternalTransactionStatus.RISK_APPROVED,
                        InternalTransactionStatus.RISK_REJECTED
                )
        );

        // Risk approved → proceed to ledger
        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.RISK_APPROVED,
                EnumSet.of(InternalTransactionStatus.LEDGER_PENDING)
        );

        // Ledger processing
        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.LEDGER_PENDING,
                EnumSet.of(
                        InternalTransactionStatus.LEDGER_SUCCESS,
                        InternalTransactionStatus.LEDGER_FAILED
                )
        );

        // Final success transition
        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.LEDGER_SUCCESS,
                EnumSet.of(InternalTransactionStatus.COMPLETED)
        );

        // Terminal states → no transitions
        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.RISK_REJECTED,
                EnumSet.noneOf(InternalTransactionStatus.class)
        );

        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.LEDGER_FAILED,
                EnumSet.noneOf(InternalTransactionStatus.class)
        );

        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.COMPLETED,
                EnumSet.noneOf(InternalTransactionStatus.class)
        );

        ALLOWED_TRANSITIONS.put(
                InternalTransactionStatus.FAILED,
                EnumSet.noneOf(InternalTransactionStatus.class)
        );
    }

    /**
     * Validates whether a transition from current → next is allowed.
     *
     * @param current current internal state
     * @param next    next intended internal state
     * @throws InvalidTransactionStateException if transition is invalid
     */
    public static void validateTransition(
            InternalTransactionStatus current,
            InternalTransactionStatus next
    ) {

        Set<InternalTransactionStatus> allowedNextStates = ALLOWED_TRANSITIONS.get(current);

        // Defensive check (should never happen if all states mapped)
        if (allowedNextStates == null) {
            throw new InvalidTransactionStateException(
                    "No transition rules defined for state: " + current
            );
        }

        // Validate transition
        if (!allowedNextStates.contains(next)) {
            throw new InvalidTransactionStateException(
                    "Invalid transition from " + current + " to " + next
            );
        }
    }
}