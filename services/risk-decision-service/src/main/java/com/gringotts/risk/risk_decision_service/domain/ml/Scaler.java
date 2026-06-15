package com.gringotts.risk.risk_decision_service.domain.ml;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Scaler {

    private Map<String, Double> mean;
    private Map<String, Double> std;

    public double standardize(String feature, double value) {

        if (mean == null || std == null) {
            throw new IllegalStateException("Scaler is not properly initialized");
        }

        Double featureMean = mean.get(feature);
        Double featureStd = std.get(feature);

        if (featureMean == null) {
            throw new IllegalStateException("Missing mean for feature: " + feature);
        }

        if (featureStd == null) {
            throw new IllegalStateException("Missing std for feature: " + feature);
        }

        if (featureStd == 0) {
            throw new IllegalStateException("Standard deviation is zero for feature: " + feature);
        }

        double scaled = (value - featureMean) / featureStd;

        // Clamp to safe range
        if (scaled > 10) return 10;
        if (scaled < -10) return -10;

        return scaled;
    }
}