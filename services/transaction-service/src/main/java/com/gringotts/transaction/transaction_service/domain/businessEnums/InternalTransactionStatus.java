package com.gringotts.transaction.transaction_service.domain.businessEnums;

/*internal transaction states for transaction*/
public enum InternalTransactionStatus {
    INITIATED,
    PENDING_RISK,
    REVIEW_PENDING,
    RISK_APPROVED,
    RISK_REJECTED,
    LEDGER_PENDING,
    LEDGER_SUCCESS,
    LEDGER_FAILED,
    COMPLETED,
    FAILED
}
/*
Internal → External Status Mapping

| Internal             | External     |
|---------------------|--------------|
| INITIATED           | INITIATED    |
| PENDING_RISK        | INITIATED    |
| RISK_APPROVED       | INITIATED    |
| LEDGER_PENDING      | INITIATED    |
| LEDGER_SUCCESS      | APPROVED     |
| COMPLETED           | APPROVED     |
| RISK_REJECTED       | DECLINED     |
| LEDGER_FAILED       | DECLINED     |
| FAILED              | DECLINED     |

no external review as once systems add review then we add approved or declined or keep it review
*/