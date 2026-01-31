package com.gringotts.risk.risk_decision_service.domain.decision;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.risk.risk_decision_service.domain.ml.ModelMetadata;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
// this classes gathers scores and reason
public class RiskDecisionContext {

    private final RiskDecisionRequest request;
    private final Instant evaluationTime;
    private int softScore =0;
    private double mlProbability =0.0;
    private List<String> reasonCodes = new ArrayList<>();
    private List<String> softReasonCodes = new ArrayList<>();
    private TransactionStatus finalStatus;
    private boolean declined = false;
    @JsonProperty("metadata")
    private ModelMetadata modelMetadata;
    private String policyVersion;
    private Instant policyActivatedAt;


    private RiskDecisionContext(RiskDecisionRequest request, Instant evaluationTime) {
        this.request = request;
        this.evaluationTime = evaluationTime;
    }
    // we create context by passing the request from transaction
    public static RiskDecisionContext from(RiskDecisionRequest request) {
        return new RiskDecisionContext(request,Instant.now());
    }


    public void addReason(String code) {
        this.reasonCodes.add(code);
    }

    public void triggerHardFail(String reason) {
        this.declined = true;
        this.addReason(reason);
    }

    public  boolean isNewDevice() {
        // In a real project, you'd compare the request's deviceId
        // against a database of known devices for this userId.
        // For now, we can check if it's null or a placeholder.
        return request.getDeviceId() == null || request.getDeviceId().equals("UNKNOWN");
    }

    public Instant evaluationTime() {
        return this.evaluationTime;
    }

    public void addSoftReason(String code) {
        this.softReasonCodes.add(code);
        this.reasonCodes.add(code); // Add to master list for the final response
    }

    public void attachModelMetadata(ModelMetadata modelMetadata) {
        this.modelMetadata = modelMetadata;
    }

    public void attachPolicyInfo(String version, Instant activatedAt) {
        if (this.policyVersion != null) {
            throw new IllegalStateException("Policy info already set");
        }
        this.policyVersion = version;
        this.policyActivatedAt = activatedAt;
    }
}

