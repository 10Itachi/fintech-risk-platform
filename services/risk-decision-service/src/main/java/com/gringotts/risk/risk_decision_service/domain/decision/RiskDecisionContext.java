package com.gringotts.risk.risk_decision_service.domain.decision;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.risk.risk_decision_service.domain.DerivedRiskFeature.DerivedRiskFeatures;
import com.gringotts.risk.risk_decision_service.domain.ml.ModelMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

@Getter
public class  RiskDecisionContext {

    private DerivedRiskFeatures features;
    private final RiskDecisionRequest request;
    private final Instant evaluationTime;
    private boolean mlEvaluated = false;

    // EXISTING (KEEP FOR NOW - DEPRECATED)
    //private int softScore = 0;

    // EXISTING
    private double mlProbability = 0.0;

    // NEW → UNIFIED RISK SCORE
    private double adjustedRisk = 0.0; //  FINAL COMBINED RISK

    // INTERNAL STATE
    private final Set<String> reasonCodes = new LinkedHashSet<>();
    private final Set<String> softReasonCodes = new LinkedHashSet<>();

    private boolean declined = false;
    private TransactionStatus finalStatus;
    private boolean finalStatusSet = false;

    private ModelMetadata modelMetadata;
    private String policyVersion;
    private Instant policyActivatedAt;

    private RiskDecisionContext(RiskDecisionRequest request, Instant evaluationTime) {
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.evaluationTime = Objects.requireNonNull(evaluationTime, "evaluationTime must not be null");
    }

    public static RiskDecisionContext from(RiskDecisionRequest request) {
        return new RiskDecisionContext(request, Instant.now());
    }

    public void attachDerivedFeatures(DerivedRiskFeatures features) {
        this.features = features;
    }

    public DerivedRiskFeatures getFeatures() {
        return features;
    }

    // REASONS
    public void addReason(String code) {
        if (code != null && !code.isBlank()) {
            reasonCodes.add(code);
        }
    }

    public void addSoftReason(String code) {
        if (code != null && !code.isBlank()) {
            softReasonCodes.add(code);
            reasonCodes.add(code);
        }
    }

    public List<String> getReasonCodes() {
        return new ArrayList<>(reasonCodes);
    }

    public List<String> getSoftReasonCodes() {
        return new ArrayList<>(softReasonCodes);
    }

    // OLD SOFT SCORE (KEEP TEMPORARY)
    /*public void increaseSoftScore(int value) {
        if (declined) return;
        if (value < 0) {
            throw new IllegalArgumentException("Soft score increment cannot be negative");
        }
        this.softScore += value;
    }*/


    //  NEW → RISK ADJUSTMENT METHOD
    public void adjustRisk(double delta) {

        if (declined) return;

        //  CRITICAL GUARD → ML must run first
        if (!mlEvaluated) {
            throw new IllegalStateException("ML must be evaluated before applying risk adjustments");
        }

        //  CURRENT SYSTEM: ONLY POSITIVE ADJUSTMENTS ALLOWED
        if (delta < 0) {
            throw new IllegalArgumentException("Negative risk adjustment not allowed in current system");
        }

        //  Optional: ignore no-op
        if (delta == 0) return;

        this.adjustedRisk += delta;

        //  SAFETY CLAMP (MANDATORY)
        if (this.adjustedRisk > 1.0) this.adjustedRisk = 1.0;
        if (this.adjustedRisk < 0.0) this.adjustedRisk = 0.0;
    }

    // ML PROBABILITY
    public void setMlProbability(double probability) {

        if (declined) return;

        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("ML probability must be between 0.0 and 1.0");
        }

        this.mlProbability = probability;
        mlEvaluated = true;

        //  NEW → INITIALIZE BASE RISK
        this.adjustedRisk = probability;  // 🔥 BASELINE
    }

    public void ensureMlEvaluated() {
        if (!mlEvaluated) {
            throw new IllegalStateException("ML must be evaluated before decision");
        }
    }

    // HARD FAIL
    public void triggerHardFail(String reason) {
        if (!this.declined) {
            this.declined = true;
        }
        addReason(reason);
    }

    public boolean isDeclined() {
        return declined;
    }

    // FINAL DECISION
    public void setFinalStatus(TransactionStatus status) {
        if (finalStatusSet) {
            throw new IllegalStateException("Final status already set");
        }
        this.finalStatus = Objects.requireNonNull(status);
        this.finalStatusSet = true;
    }

    // METADATA
    public void attachModelMetadata(ModelMetadata modelMetadata) {
        if (this.modelMetadata != null) {
            throw new IllegalStateException("Model metadata already attached");
        }
        this.modelMetadata = Objects.requireNonNull(modelMetadata);
    }

    public ModelMetadata getModelMetadataSafe() {
        if (this.modelMetadata == null) {
            throw new IllegalStateException("Model metadata not attached");
        }
        return this.modelMetadata;
    }

    public String getPolicyVersionSafe() {
        if (this.policyVersion == null) {
            throw new IllegalStateException("Policy version not set");
        }
        return this.policyVersion;
    }

    public Instant getPolicyActivatedAtSafe() {
        if (this.policyActivatedAt == null) {
            throw new IllegalStateException("Policy activation time not set");
        }
        return this.policyActivatedAt;
    }

    public void attachPolicyInfo(String version, Instant activatedAt) {
        if (this.policyVersion != null) {
            throw new IllegalStateException("Policy info already set");
        }
        this.policyVersion = Objects.requireNonNull(version);
        this.policyActivatedAt = Objects.requireNonNull(activatedAt);
    }

    public Instant evaluationTime() {
        return this.evaluationTime;
    }
}
/*
===========================================================
FUTURE DESIGN NOTE — RISK ADJUSTMENT STRATEGY
===========================================================

Current System (Phase 1 — Weak ML Model)
---------------------------------------
- Only POSITIVE risk adjustments are allowed.
- Soft rules act as RISK AMPLIFIERS.
- Reason:
    ML model is weak (limited features + synthetic data),
    so reducing risk may lead to false approvals (fraud leakage).

Final Risk Calculation:
    adjustedRisk = ML_probability + Σ(positive adjustments)

-----------------------------------------------------------

Future System (Phase 2 — Strong ML Model)
----------------------------------------
- Both POSITIVE and NEGATIVE adjustments can be introduced.

Updated Formula:
    finalRisk = ML_score
              + Σ(risk_boosts)
              - Σ(trust_discounts)

-----------------------------------------------------------

Why Negative Adjustments Are Needed
----------------------------------
- Reduce false positives
- Improve customer experience
- Incorporate TRUST signals such as:
    - Known device
    - Stable geo-location
    - Consistent user behavior
    - Historical transaction patterns

-----------------------------------------------------------

Important Constraints for Negative Adjustments
---------------------------------------------
- Must be SMALL and controlled
- Must be backed by strong signals (not heuristics)
- Must NOT overpower ML signal

Example Safeguards:
    finalRisk ≥ ML_score * 0.7   (prevent over-discounting)

-----------------------------------------------------------

Design Philosophy
-----------------
- ML = Primary risk signal
- Soft Rules = Adjustment layer (not competing system)
- Hard Rules = Absolute gatekeepers

-----------------------------------------------------------

Migration Strategy
------------------
1. Introduce negative adjustments only after:
    - Strong model validation
    - Real production data
    - Monitoring of false positives/negatives

2. Add:
    - trustScore
    - behavioral profiling
    - feature store integration

===========================================================
*/