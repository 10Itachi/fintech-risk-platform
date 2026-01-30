package com.gringotts.risk.risk_decision_service.domain.ml;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import org.springframework.stereotype.Component;
@Component
public class MlFeatureBuilder {

    public MlFeatureVector build(RiskDecisionContext ctx) {
        MlFeatureVector vector = new MlFeatureVector();
        var r = ctx.getRequest();

        // 1. Numeric Features
        vector.put("amount", r.getAmount().doubleValue());
        vector.put("total_amount_last_24h", r.getTotalAmountLast24h().doubleValue());
        vector.put("txn_count_last_24h", (double) r.getTxnCountLast24h());

        // 2. Boolean Features (Mapped to 1.0 or 0.0)
        vector.put("is_new_device", ctx.isNewDevice() ? 1.0 : 0.0);

        // 3. Categorical Features (One-Hot Encoding)
        // These MUST match the keys in your JSON coefficients
        vector.put("channel_" + r.getChannel().name(), 1.0);
        vector.put("transaction_type_" + r.getTransactionType().name(), 1.0);
        vector.put("country_" + r.getCountry(), 1.0);

        return vector;
    }
}




