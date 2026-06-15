# README.md

# ML Risk Model Pipeline

## Overview

This module powers the fraud-risk intelligence layer of the Gringotts banking platform.

The pipeline is responsible for:

- Generating realistic transaction datasets
- Engineering ML-ready behavioral features
- Simulating fraud behavior patterns
- Training fraud detection models
- Exporting production-safe model artifacts for Java inference

The generated model artifact is consumed by:

- `risk-decision-service`

The architecture is intentionally designed to maintain:

- Python ↔ Java feature parity
- deterministic model exports
- reproducible local execution
- future containerized deployment support

---

# Pipeline Responsibilities

| Stage                         | Responsibility                                     |
| ----------------------------- | -------------------------------------------------- |
| user_transaction_simulator.py | Generates realistic synthetic banking transactions |
| behavior_features.py          | Builds ML-ready behavioral features                |
| label_generator.py            | Assigns fraud probabilities and labels             |
| dataset_validator.py          | Validates dataset realism and feature quality      |
| train_model.py                | Trains ML fraud detection model                    |
| model_exporter.py             | Exports Java-compatible JSON model                 |

---

# Generated Artifacts

| File                           | Purpose                    |
| ------------------------------ | -------------------------- |
| artifacts/raw_transactions.csv | Raw simulated transactions |
| artifacts/feature_dataset.csv  | Engineered feature dataset |
| artifacts/final_dataset.csv    | Labeled fraud dataset      |
| output/active-model.json       | Active production model    |
| output/model-\*.json           | Historical model snapshots |

---

# Design Principles

## Java Compatibility

The exported JSON model is designed specifically for:

- Java backend inference
- deterministic feature ordering
- production-safe scoring

---

## Feature Parity

The feature engineering logic mirrors:

- Java risk feature builders
- device history logic
- transaction velocity calculations

This ensures inference consistency between:

- Python training
- Java runtime scoring

---

# Docker Support

The module is fully containerized using Docker.

The container:

- installs Python dependencies
- executes the pipeline
- produces model artifacts

Default runtime:

```bash
python run_pipeline.py
```

---

# Seeder Scripts

The project also includes dataset seeding utilities for:

- fraud scenario simulation
- transaction history generation
- edge-case testing

Seeder scripts are intended for:

- local testing
- integration testing
- fraud rule validation

They are NOT automatically executed during pipeline startup.

---

# Future Improvements

- S3-backed model storage
- scheduled retraining
- model registry integration
- feature store integration
- drift detection
- distributed training support
