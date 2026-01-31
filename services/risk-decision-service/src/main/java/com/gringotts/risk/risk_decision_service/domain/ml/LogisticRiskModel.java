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
        MlFeatureVector features = featureBuilder.build(ctx);
        return model.predict(features);
    }
    public MlModelArtifact getModel() {
        return this.model;
    }
}

