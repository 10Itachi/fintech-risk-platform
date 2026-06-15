package com.gringotts.risk.risk_decision_service.domain.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MlModelArtifact {

    @JsonProperty("metadata")
    private ModelMetadata metadata;

    @JsonProperty("model_type")
    private String modelType;

    private double intercept;

    private Map<String, Double> coefficients;

    @JsonProperty("feature_order")
    private List<String> feature_order;

    private Scaler scaler;

    public double predict(MlFeatureVector features) {

        // VALIDATION: BASIC CHECKS
        if (features == null) {
            throw new IllegalArgumentException("Feature vector cannot be null");
        }

        if (feature_order == null || feature_order.isEmpty()) {
            throw new IllegalStateException("Feature order is not initialized");
        }

        if (coefficients == null) {
            throw new IllegalStateException("Model coefficients are not initialized");
        }

        if (scaler == null) {
            throw new IllegalStateException("Scaler is not initialized");
        }

        // VALIDATE NUMERIC FEATURES
        for (String featureName : feature_order) {

            if (scaler.getMean().containsKey(featureName)) {
                if (!features.contains(featureName)) {
                    throw new IllegalStateException("Missing numeric feature: " + featureName);
                }
            }
        }

        // VALIDATE CATEGORICAL GROUPS
        validateOneHotGroup(features, "channel_");
        validateOneHotGroup(features, "transaction_type_");
        validateOneHotGroup(features, "country_");

        // MODEL COMPUTATION
        double z = this.intercept;

        for (String featureName : feature_order) {

            double rawValue = features.get(featureName);
            double processedValue = rawValue;

            // Apply scaling for numeric features
            if (scaler.getMean().containsKey(featureName)) {
                processedValue = scaler.standardize(featureName, rawValue);
            }

            Double weight = coefficients.get(featureName);

            if (weight == null) {
                throw new IllegalStateException("Missing coefficient for feature: " + featureName);
            }

            z += weight * processedValue;
        }

        return sigmoid(z);
    }

    // HELPER: ONE-HOT VALIDATION
    private void validateOneHotGroup(MlFeatureVector features, String prefix) {

        boolean found = false;

        for (String featureName : feature_order) {
            if (featureName.startsWith(prefix) && features.contains(featureName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalStateException("No feature set for category group: " + prefix);
        }
    }

    // SIGMOID (SAFE)
    private double sigmoid(double z) {

        // Hard clamp (optional but safe)
        if (z > 500) return 1.0;
        if (z < -500) return 0.0;

        if (z >= 0) {
            double expNegZ = Math.exp(-z);
            return 1.0 / (1.0 + expNegZ);
        } else {
            double expZ = Math.exp(z);
            return expZ / (1.0 + expZ);
        }
    }
}