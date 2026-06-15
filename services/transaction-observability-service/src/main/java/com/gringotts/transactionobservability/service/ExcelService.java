package com.gringotts.transactionobservability.service;

import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;

import java.util.List;

public interface ExcelService {
    byte[] generateTransactionExcel(List<TransactionEventRecord> data);
}
