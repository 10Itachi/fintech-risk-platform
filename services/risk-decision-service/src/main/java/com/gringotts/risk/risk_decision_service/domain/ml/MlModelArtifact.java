package com.gringotts.risk.risk_decision_service.domain.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
@Getter
@Setter
public class MlModelArtifact {
    @JsonProperty("metadata")
    private ModelMetadata metadata;
    private double intercept;
    private Map<String, Double> coefficients;
    @JsonProperty("feature_order")
    private List<String> feature_order;
    private Scaler scaler;

    public double predict(MlFeatureVector features) {
        // Start with the base intercept (the 'bias' from your JSON)
        double z = this.intercept;

        for (String featureName : feature_order) {
            double rawValue = features.get(featureName);
            double processedValue = rawValue;

            // Apply Z-score scaling ONLY if the feature is numeric (amount, velocity, etc.)
            // Based on your JSON, scaler.mean only contains numeric keys
            if (scaler.getMean().containsKey(featureName)) {
                processedValue = scaler.standardize(featureName, rawValue);
            }

            // weight * value
            z += coefficients.getOrDefault(featureName, 0.0) * processedValue;
        }

        // Pass through Sigmoid to get probability (0.0 to 1.0)
        return sigmoid(z);
    }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

}