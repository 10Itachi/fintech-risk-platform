# ML_PIPELINE.md

# ML Fraud Detection Pipeline

## Pipeline Flow

The fraud detection training flow follows a staged data-processing architecture.

---

# Stage 1 — Transaction Simulation

Script:

```text
user_transaction_simulator.py
```

Purpose:

- Generate realistic banking transaction history
- Simulate user behavior profiles
- Simulate channel preferences
- Simulate transaction velocity patterns

Output:

```text
artifacts/raw_transactions.csv
```

---

# Stage 2 — Feature Engineering

Script:

```text
behavior_features.py
```

Purpose:

- Build ML-ready behavioral features
- Simulate device-history tracking
- Generate transaction velocity metrics
- Maintain parity with Java runtime logic

Generated Features:

- amount
- txn_count_last_24h
- total_amount_last_24h
- is_new_device
- is_odd_hour
- channel
- transaction_type
- country

Output:

```text
artifacts/feature_dataset.csv
```

---

# Stage 3 — Fraud Label Generation

Script:

```text
label_generator.py
```

Purpose:

- Simulate realistic fraud probabilities
- Generate fraud labels using weighted heuristics
- Maintain realistic fraud-rate distribution

Fraud Signals Include:

- high transaction amount
- transaction velocity spikes
- abnormal device usage
- unusual transaction timing

Output:

```text
artifacts/final_dataset.csv
```

---

# Stage 4 — Dataset Validation

Script:

```text
dataset_validator.py
```

Purpose:

- Validate fraud-rate realism
- Validate feature separation quality
- Detect poor feature distributions
- Verify dataset sanity before training

Validation Includes:

- class balance checks
- feature separation checks
- fraud distribution analysis

---

# Stage 5 — Model Training

Script:

```text
train_model.py
```

Model:

- Logistic Regression

Preprocessing:

- missing-value handling
- log transformations
- categorical encoding
- feature scaling

Metrics:

- accuracy
- precision
- recall
- ROC-AUC
- confusion matrix

---

# Stage 6 — Model Export

Script:

```text
model_exporter.py
```

Purpose:

- Export trained model into Java-compatible JSON
- Preserve deterministic feature mappings
- Enable production-safe inference

Output:

```text
output/active-model.json
```

Historical model versions are retained automatically.

---

# Java Integration

The exported model artifact is consumed by:

```text
risk-decision-service
```

The Java service:

- loads active-model.json
- builds runtime features
- performs fraud-risk scoring
- applies business risk rules

---

# Operational Design

The ML pipeline is intentionally separated from:

- transaction runtime services
- request-processing services

This allows:

- independent retraining
- future scheduled execution
- future cloud-native deployment
- future object-storage integration
