package com.gringotts.risk.risk_decision_service.domain.decision;

import com.gringotts.enums.TransactionStatus;
import com.gringotts.risk.risk_decision_service.domain.ml.ModelMetadata;

import java.time.Instant;
import java.util.List;

public class RiskDecisionTrace {

    private final TransactionStatus finalStatus;
    //private final int softRiskScore;
    private final double mlProbability;
    private final List<String> reasonCodes;
    private final Instant evaluatedAt;
    private final ModelMetadata modelMetadata;
    private final  String policyVersion;

    public RiskDecisionTrace(
            TransactionStatus finalStatus,
            double mlProbability,
            List<String> reasonCodes,
            Instant evaluatedAt, ModelMetadata modelMetadata, String policyVersion
    ) {
        this.finalStatus = finalStatus;
        //this.softRiskScore = softRiskScore;
        this.mlProbability = mlProbability;
        this.reasonCodes = List.copyOf(reasonCodes);
        this.evaluatedAt = evaluatedAt;
        this.modelMetadata = modelMetadata;
        this.policyVersion = policyVersion;
    }

    public TransactionStatus getFinalStatus() {
        return finalStatus;
    }

    /*public int getSoftRiskScore() {
        return softRiskScore;
    }*/

    public double getMlProbability() {
        return mlProbability;
    }

    public List<String> getReasonCodes() {
        return reasonCodes;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }
    public ModelMetadata getModelMetadata() {
        return modelMetadata;
    }
    public String getPolicyVersion() {return  policyVersion;}
}
