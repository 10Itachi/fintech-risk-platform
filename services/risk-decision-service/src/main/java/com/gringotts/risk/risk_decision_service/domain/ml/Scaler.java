package com.gringotts.risk.risk_decision_service.domain.ml;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
public class Scaler {
    private Map<String, Double> mean;
    private Map<String, Double> std; // Matches "std" in your JSON

    public double standardize(String feature, double value) {
        Double featureMean = mean.get(feature);
        Double featureStd = std.get(feature);

        if (featureMean == null || featureStd == null || featureStd == 0) {
            return value;
        }

        // Z-score formula: (x - mean) / std
        return (value - featureMean) / featureStd;
    }
}
