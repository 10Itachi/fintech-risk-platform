# ==========================================
# MODEL EXPORTER (PYTHON → JAVA) - FINAL
# ==========================================

"""
PURPOSE:
--------
Convert trained model into JSON format for Java inference.

KEY DESIGN:
-----------
- Explicit numeric feature mapping (NO index assumptions)
- Stable, production-safe export
- Ensures Python ↔ Java feature parity

OUTPUT:
-------
shared_model/
    ├── model-<version>.json
    └── active-model.json
"""

import json
from datetime import datetime
from pathlib import Path

# ==========================================
# OUTPUT FILES
# ==========================================

PROJECT_ROOT = Path(__file__).parent.parent

OUTPUT_DIR = PROJECT_ROOT / "output"

ACTIVE_MODEL_FILE = OUTPUT_DIR / "active-model.json"

MAX_MODEL_VERSIONS = 2

# ==========================================
# FEATURE CONFIG
# ==========================================

NUMERIC_FEATURES = [
    "amount",
    "total_amount_last_24h",
    "txn_count_last_24h"
]

# ==========================================
# CLEANUP OLD MODELS
# ==========================================

def cleanup_old_models():
    """
    Keeps only latest N model versions.
    """

    model_files = sorted(
        OUTPUT_DIR.glob("model-*.json"),
        key=lambda f: f.stat().st_mtime,
        reverse=True
    )

    old_models = model_files[MAX_MODEL_VERSIONS:]

    for old_model in old_models:
        old_model.unlink()
        print(f"Deleted old model: {old_model.name}")

# ==========================================
# EXPORT FUNCTION
# ==========================================

def export_model(model, scaler, feature_columns):
    """
    Converts trained model into JSON artifact for Java inference.
    """

    MODEL_NAME = "fraud_detection_logistic_regression"

    BASE_VERSION = "2.0.0"

    TIMESTAMP = datetime.now().strftime("%Y%m%d-%H%M%S")

    FULL_VERSION = f"{BASE_VERSION}-{TIMESTAMP}"

    VERSIONED_MODEL_FILE = (
        OUTPUT_DIR /
        f"model-{FULL_VERSION}.json"
    )

    # ==========================================
    # BUILD COEFFICIENT MAP
    # ==========================================

    coefficients = {
        feature: float(coef)
        for feature, coef in zip(feature_columns, model.coef_[0])
    }

    # ==========================================
    # BUILD SCALER MAP
    # ==========================================

    scaler_mean = {}

    scaler_std = {}

    for i, feature in enumerate(NUMERIC_FEATURES):

        scaler_mean[feature] = float(scaler.mean_[i])

        scaler_std[feature] = float(scaler.scale_[i])

    # ==========================================
    # BUILD ARTIFACT
    # ==========================================

    artifact = {

        "metadata": {
            "model_name": MODEL_NAME,
            "model_version": FULL_VERSION,
            "trained_at": datetime.now().isoformat()
        },

        "model_type": "logistic_regression",

        # model parameters
        "intercept": float(model.intercept_[0]),

        "coefficients": coefficients,

        # scaler values
        "scaler": {
            "mean": scaler_mean,
            "std": scaler_std
        },

        # must match java feature vector order
        "feature_order": feature_columns
    }

    # ==========================================
    # CREATE SHARED DIRECTORY
    # ==========================================

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # ==========================================
    # SAVE VERSIONED MODEL
    # ==========================================

    with open(VERSIONED_MODEL_FILE, "w") as f:
        json.dump(artifact, f, indent=4)

    # ==========================================
    # UPDATE ACTIVE MODEL
    # ==========================================

    with open(ACTIVE_MODEL_FILE, "w") as f:
        json.dump(artifact, f, indent=4)

    # ==========================================
    # CLEANUP OLD MODELS
    # ==========================================

    cleanup_old_models()

    print("\nModel exported successfully")

    print(f"Saved versioned model: {VERSIONED_MODEL_FILE}")

    print(f"Updated active model: {ACTIVE_MODEL_FILE}")

    print(f"Version: {FULL_VERSION}")

# ==========================================
# ENTRY POINT
# ==========================================

if __name__ == "__main__":
    print("Run this via train_model pipeline")

