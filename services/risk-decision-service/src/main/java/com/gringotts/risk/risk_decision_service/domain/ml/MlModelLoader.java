package com.gringotts.risk.risk_decision_service.domain.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


// spring boot starts and creates this class  loads/reads active-model.json
//Converts JSON → Java object, validates model for all the mata data and other values
// and stores model in memory and output modelArtifact

@Getter
@Component
public class MlModelLoader {

    private final MlModelArtifact model;
    private final String modelPath;

    public MlModelLoader(ObjectMapper mapper, @Value("${ml.model.path}")String modelPath) {
        this.modelPath = modelPath;

        Path path = Paths.get(modelPath);

        if (!Files.exists(path)) {
            throw new IllegalStateException(
                    "ML model file not found: " + modelPath
            );
        }

        try {

            String json = Files.readString(path);

            // load model
            this.model = mapper.readValue(
                    json,
                    MlModelArtifact.class
            );

            // validate model immediately
            validateModel(this.model);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to load ML model from: " + modelPath,
                    e
            );
        }
    }


    // MODEL VALIDATION
    private void validateModel(MlModelArtifact model) {

        if (model == null) {
            throw new IllegalStateException("Model cannot be null");
        }

        // METADATA
        if (model.getMetadata() == null) {
            throw new IllegalStateException("Model metadata is missing");
        }

        if (model.getMetadata().getModelName() == null ||
                model.getMetadata().getModelVersion() == null ||
                model.getMetadata().getTrainedAt() == null) {
            throw new IllegalStateException("Model metadata fields are incomplete");
        }

        if (!model.getMetadata().getModelVersion().startsWith("2.")) {
            throw new IllegalStateException("Unsupported model version: " + model.getMetadata().getModelVersion());
        }
        if (model.getModelType() == null ||
                !"logistic_regression".equalsIgnoreCase(model.getModelType())) {
            throw new IllegalStateException("Unsupported model type: " + model.getModelType());
        }

        // FEATURE ORDER
        if (model.getFeature_order() == null || model.getFeature_order().isEmpty()) {
            throw new IllegalStateException("Feature order is missing or empty");
        }
        long uniqueCount = model.getFeature_order().stream().distinct().count();

        if (uniqueCount != model.getFeature_order().size()) {
            throw new IllegalStateException("Duplicate features found in feature_order");
        }

        // COEFFICIENTS
        if (model.getCoefficients() == null || model.getCoefficients().isEmpty()) {
            throw new IllegalStateException("Model coefficients are missing or empty");
        }
        for (String coefKey : model.getCoefficients().keySet()) {
            if (!model.getFeature_order().contains(coefKey)) {
                throw new IllegalStateException("Coefficient contains unknown feature: " + coefKey);
            }
        }

        // INTERCEPT
        if (Double.isNaN(model.getIntercept()) || Double.isInfinite(model.getIntercept())) {
            throw new IllegalStateException("Invalid intercept value");
        }

        // SCALER
        if (model.getScaler() == null) {
            throw new IllegalStateException("Scaler is missing");
        }

        if (model.getScaler().getMean() == null || model.getScaler().getStd() == null) {
            throw new IllegalStateException("Scaler mean/std is missing");
        }

        if (model.getScaler().getMean().isEmpty() || model.getScaler().getStd().isEmpty()) {
            throw new IllegalStateException("Scaler mean/std cannot be empty");
        }

        // FEATURE ↔ COEFFICIENT MATCH
        for (String feature : model.getFeature_order()) {
            if (!model.getCoefficients().containsKey(feature)) {
                throw new IllegalStateException("Missing coefficient for feature: " + feature);
            }
        }

        // SCALER CONSISTENCY
        if (!model.getScaler().getMean().keySet().equals(model.getScaler().getStd().keySet())) {
            throw new IllegalStateException("Scaler mean and std keys mismatch");
        }

        for (String feature : model.getScaler().getMean().keySet()) {
            if (!model.getFeature_order().contains(feature)) {
                throw new IllegalStateException("Scaler mean contains unknown feature: " + feature);
            }
        }

        for (String feature : model.getScaler().getStd().keySet()) {
            if (!model.getFeature_order().contains(feature)) {
                throw new IllegalStateException("Scaler std contains unknown feature: " + feature);
            }
        }
        for (String feature : model.getScaler().getMean().keySet()) {
            if (!model.getCoefficients().containsKey(feature)) {
                throw new IllegalStateException("Scaler contains feature not in coefficients: " + feature);
            }
        }
    }
}
