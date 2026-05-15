package com.gringotts.risk.risk_decision_service.domain.ml;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class LogisticRiskModel {

    private final MlModelArtifact model;
    private final MlFeatureBuilder featureBuilder;

    public LogisticRiskModel(MlModelLoader loader,
                             MlFeatureBuilder featureBuilder) {
        this.model = loader.getModel();
        this.featureBuilder = featureBuilder;
    }

    public double score(RiskDecisionContext ctx) {

        // CONTEXT VALIDATION
        if (ctx == null) {
            throw new IllegalArgumentException("RiskDecisionContext cannot be null");
        }

        if (ctx.getRequest() == null) {
            throw new IllegalStateException("Request missing in context");
        }

        if (ctx.getFeatures() == null) {
            throw new IllegalStateException("Derived features not attached to context before ML scoring");
        }

        // FEATURE BUILDING
        MlFeatureVector features = featureBuilder.build(ctx);

        // FEATURE VALIDATION
        validateFeatureCoverage(features);

        // PREDICTION
        return model.predict(features);
    }

    private void validateFeatureCoverage(MlFeatureVector features) {

        // EXACT FEATURE MATCH CHECK
        for (String feature : model.getFeature_order()) {
            if (!features.contains(feature)) {
                throw new IllegalStateException("Missing feature from builder: " + feature);
            }
        }


        // NO EXTRA FEATURES CHECK
        for (String builtFeature : features.getFeatureNames()) {
            if (!model.getFeature_order().contains(builtFeature)) {
                throw new IllegalStateException("Unexpected feature generated: " + builtFeature);
            }
        }

        // NUMERIC FEATURES
        for (String feature : model.getFeature_order()) {

            if (model.getScaler().getMean().containsKey(feature)) {
                if (!features.contains(feature)) {
                    throw new IllegalStateException("Missing numeric feature in builder: " + feature);
                }
            }
        }

        // CATEGORICAL GROUPS
        validateOneHotGroup(features, "channel_");
        validateOneHotGroup(features, "transaction_type_");
        validateOneHotGroup(features, "country_");
    }

    private void validateOneHotGroup(MlFeatureVector features, String prefix) {

        boolean found = false;

        for (String featureName : model.getFeature_order()) {
            if (featureName.startsWith(prefix) && features.contains(featureName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalStateException("Missing categorical feature group: " + prefix);
        }
    }
    public MlModelArtifact getModel() {
        return this.model;
    }
}

