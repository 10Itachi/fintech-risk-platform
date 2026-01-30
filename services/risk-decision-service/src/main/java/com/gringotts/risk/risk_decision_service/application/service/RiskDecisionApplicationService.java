package com.gringotts.risk.risk_decision_service.application.service;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.risk.risk_decision_service.api.controller.RiskEvaluationController;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionPolicy;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionTrace;
import com.gringotts.risk.risk_decision_service.domain.ml.MlScoringService;
import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceMapper;
import com.gringotts.risk.risk_decision_service.domain.rule.hardrule.HardRuleEngine;
import com.gringotts.risk.risk_decision_service.domain.rule.softrule.SoftRuleEngine;
import com.gringotts.risk.risk_decision_service.infrastructure.repository.RiskDecisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class  RiskDecisionApplicationService {
    private final Logger LOGGER = LoggerFactory.getLogger(RiskEvaluationController.class);
    private final HardRuleEngine hardRuleEngine;
    private final SoftRuleEngine softRuleEngine;
    private final MlScoringService mlScoringService;
    private final RiskDecisionPolicy decisionPolicy; // Added
    private final RiskDecisionMapper mapper;
    private final RiskDecisionRepository riskDecisionRepository;
    private  final RiskDecisionTraceMapper traceMapper;


    public RiskDecisionApplicationService(
            HardRuleEngine hardRuleEngine, SoftRuleEngine softRuleEngine,
            MlScoringService mlScoringService, RiskDecisionPolicy decisionPolicy,
            RiskDecisionMapper mapper, RiskDecisionRepository riskDecisionRepository, RiskDecisionTraceMapper traceMapper) {
        this.hardRuleEngine = hardRuleEngine;
        this.softRuleEngine = softRuleEngine;
        this.mlScoringService = mlScoringService;
        this.decisionPolicy = decisionPolicy;
        this.mapper = mapper;
        this.riskDecisionRepository = riskDecisionRepository;
        this.traceMapper = traceMapper;


    }

    public RiskDecisionResponse evaluate(RiskDecisionRequest request) {
        LOGGER.info("******-inside the risk-decision-service application");
        RiskDecisionContext ctx = RiskDecisionContext.from(request);
        LOGGER.info("******-got the context");
        // 1️⃣ Hard rules (Immediate exit if triggered)
        hardRuleEngine.evaluate(ctx).ifPresent(ctx::triggerHardFail);

        if (!ctx.isDeclined()) {
            // 2️⃣ Soft rules (Accumulate evidence)
            softRuleEngine.evaluate(ctx);

            // 3️⃣ ML Scoring (Probability 0.0 - 1.0)
            mlScoringService.evaluate(ctx);
        }

        // 4️⃣ Final Decision based on Policy
        ctx.setFinalStatus(decisionPolicy.decide(ctx));
        RiskDecisionTrace trace = new RiskDecisionTrace(
                ctx.getFinalStatus(),
                ctx.getSoftScore(),
                ctx.getMlProbability(),
                ctx.getReasonCodes(),
                ctx.evaluationTime()
        );
        RiskDecisionTraceEntity traceEntity = traceMapper.toEntity(trace,request);
        riskDecisionRepository.save(traceEntity);
        LOGGER.info("******-got the entity");
        // 3. Persist (async later, sync for now)
        return mapper.toResponse(ctx);
    }


}

