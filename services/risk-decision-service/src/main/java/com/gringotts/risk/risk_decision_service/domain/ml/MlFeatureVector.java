package com.gringotts.risk.risk_decision_service.domain.ml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MlFeatureVector {

    private final Map<String, Double> values;
    private boolean locked = false;

    public MlFeatureVector() {
        this.values = new HashMap<>();
    }

    // WRITE (ONLY BEFORE LOCK)
    public void put(String key, double value) {

        if (locked) {
            throw new IllegalStateException("Cannot modify feature vector after build");
        }

        values.put(key, value);
    }

    // LOCK (MAKE IMMUTABLE)
    public void lock() {
        this.locked = true;
    }

    // READ
    public double get(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalStateException("Missing feature: " + key);
        }
        return values.get(key);
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }

    // EXPOSE KEYS (Issue 15 prep)
    public Set<String> getFeatureNames() {
        return Collections.unmodifiableSet(values.keySet());
    }

    @Override
    public String toString() {
        return values.toString();
    }

    public Map<String, Double> asMap() {
        return Collections.unmodifiableMap(values);
    }
}