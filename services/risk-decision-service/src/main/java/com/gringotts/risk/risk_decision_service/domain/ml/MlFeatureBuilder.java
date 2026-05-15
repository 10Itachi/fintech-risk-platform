package com.gringotts.risk.risk_decision_service.domain.ml;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MlFeatureBuilder {

    private static final Logger log = LoggerFactory.getLogger(MlFeatureBuilder.class);
    private final MlModelArtifact model;

    public MlFeatureBuilder(MlModelLoader loader) {
        this.model = loader.getModel();
    }


    public MlFeatureVector build(RiskDecisionContext ctx) {

        //0.CONTEXT VALIDATION
        if (ctx == null) {
            throw new IllegalArgumentException("RiskDecisionContext cannot be null");
        }

        if (ctx.getRequest() == null) {
            throw new IllegalStateException("Request cannot be null in context");
        }

        if (ctx.getFeatures() == null) {
            throw new IllegalStateException("Derived features not attached to context");
        }

        MlFeatureVector vector = new MlFeatureVector();
        for (String featureName : model.getFeature_order()) {
            vector.put(featureName, 0.0);
        }
        var r = ctx.getRequest();

        // 1. Numeric Features
        //validation
        if (r.getAmount() == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (r.getTotalAmountLast24h() == null) {
            throw new IllegalArgumentException("totalAmountLast24h cannot be null");
        }

        if (r.getTxnCountLast24h() == null) {
            throw new IllegalArgumentException("txnCountLast24h cannot be null");
        }

        // 1. Numeric Features (WITH LOG TRANSFORM - MUST MATCH PYTHON)
        vector.put("amount", Math.log1p(Math.max(r.getAmount().doubleValue(), 0)));
        vector.put("total_amount_last_24h", Math.log1p(Math.max(r.getTotalAmountLast24h().doubleValue(), 0)));
        vector.put("txn_count_last_24h", (double) r.getTxnCountLast24h());

        // 2. Boolean Features (Mapped to 1.0 or 0.0)
        vector.put("is_new_device", ctx.getFeatures().isNewDevice() ? 1.0 : 0.0);
        vector.put("is_odd_hour", ctx.getFeatures().isOddHour() ? 1.0 : 0.0);

        // 3. Categorical Features (One-Hot Encoding)

        // 4. Map Categorical Features (One-Hot Encoding)
        // We overwrite the previously set 0.0 with 1.0 ONLY if the feature is known.
        mapCategoricalFeature(vector, "channel_" + r.getChannel().name());
        mapCategoricalFeature(vector, "transaction_type_" + r.getTransactionType().name());
        mapCategoricalFeature(vector, "country_" + r.getCountry());

        log.debug("Feature vector built successfully for txnId: {}", r.getTransactionId());

        vector.lock(); // Prevent accidental modification after building
        return vector;
    }

    /**
     * Safely maps a categorical feature. If the feature is unknown to the model,
     * it logs a warning but keeps the value at 0.0 (baseline), preventing a crash.
     */
    private void mapCategoricalFeature(MlFeatureVector vector, String featureKey) {
        if (model.getFeature_order().contains(featureKey)) {
            vector.put(featureKey, 1.0);
        } else {
            // In Production, this triggers an alert to update the model with new data
            log.warn("Unknown categorical feature encountered: {} - Feature skipped (Baseline applied)", featureKey);
        }
    }

}





