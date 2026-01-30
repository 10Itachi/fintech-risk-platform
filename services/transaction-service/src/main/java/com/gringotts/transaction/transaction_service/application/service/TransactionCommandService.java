package com.gringotts.transaction.transaction_service.application.service;
import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.transaction.transaction_service.api.dto.request.TransactionRequestDto;
import com.gringotts.transaction.transaction_service.api.dto.response.TransactionResponseDto;
import com.gringotts.transaction.transaction_service.application.mapper.RiskRequestMapper;
import com.gringotts.transaction.transaction_service.application.mapper.TransactionMapper;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.domain.policy.TransactionStateMachine;
import com.gringotts.transaction.transaction_service.infrastructure.repository.TransactionRepository;
import com.gringotts.transaction.transaction_service.security.jwt.JwtContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransactionCommandService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final RiskRequestMapper riskRequestMapper;
    private final RiskOrchestrationService riskOrchestrationService;
    private final TransactionRiskFeatureService riskFeatureService;
    public TransactionCommandService(TransactionMapper transactionMapper, TransactionRepository transactionRepository, RiskRequestMapper riskRequestMapper, RiskOrchestrationService riskOrchestrationService, TransactionRiskFeatureService riskFeatureService) {
        this.transactionMapper = transactionMapper;
        this.transactionRepository = transactionRepository;
        this.riskRequestMapper = riskRequestMapper;
        this.riskOrchestrationService = riskOrchestrationService;
        this.riskFeatureService = riskFeatureService;
    }

    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto transactionRequestDto ) {
        Long userId = JwtContextHolder.getUserId();
        Transaction transaction = transactionMapper.toEntity(transactionRequestDto);
        transaction.setUserId(userId);
        transaction.setTransactionStatus(TransactionStatus.INITIATED);
        transaction.setCreatedAt(Instant.now());
        /*calculating and setting 24H parameters*/
        BigDecimal totalAmountLast24H = riskFeatureService
                .totalAmountLast24H(transaction.getUserId(), transaction.getCreatedAt());
        Integer numberOfTransactionsLast24h = riskFeatureService
                .numberOfTransactionsLast24h(transaction.getUserId(), transaction.getCreatedAt());
        transaction.setTotalAmountLast24h(totalAmountLast24H);
        transaction.setTxnCountLast24h(numberOfTransactionsLast24h);
        transactionRepository.save(transaction);
        LOGGER.info("******-Transaction saved and INITIATED successfully in transaction repository"+
                "\n"+transaction.getTransactionId()+
                "\n"+transaction.getTransactionStatus());

        RiskDecisionRequest riskDecisionRequest = riskRequestMapper.toRiskRequest(transaction,totalAmountLast24H,numberOfTransactionsLast24h,transaction.getCreatedAt());
        RiskDecisionResponse riskDecisionResponse = riskOrchestrationService.evaluateRiskIndependent(riskDecisionRequest);

        TransactionStateMachine.validateTransition(transaction.getTransactionStatus(),riskDecisionResponse.getTransactionStatus());
        transaction.setTransactionStatus(riskDecisionResponse.getTransactionStatus());
        transactionRepository.save(transaction);
        LOGGER.info("******-Transaction saved after the risk analysis: "
                +"\n"+transaction.getTransactionId()
                +"\n"+transaction.getTransactionStatus());
        return transactionMapper.toDto(transaction);
    }

}
