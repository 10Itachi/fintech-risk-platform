package com.gringotts.risk.risk_decision_service.domain.ml;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


public class MlFeatureVector {

    private final Map<String, Double> values = new HashMap<>();

    public void put(String key, double value) {
        values.put(key, value);
    }

    public double get(String key) {
        return values.getOrDefault(key, 0.0);
    }
}
