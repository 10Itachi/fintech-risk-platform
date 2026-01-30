import json
import pandas as pd
import numpy as np
from pathlib import Path
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LogisticRegression

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_FILE = BASE_DIR / "artifacts" / "transactions.csv"
MODEL_FILE = BASE_DIR / "artifacts" / "logistic_model.json"

def train():
    df = pd.read_csv(DATA_FILE)

    # 1. Feature Engineering
    df["is_new_device"] = df["device_id"].apply(
        lambda x: 1 if str(x).startswith("NEW") or x == "UNKNOWN" else 0
    )

    NUMERIC = ["amount", "total_amount_last_24h", "txn_count_last_24h"]
    CATEGORICAL = ["channel", "transaction_type", "country"]
    
    # 2. Encoding
    X = pd.get_dummies(df[NUMERIC + CATEGORICAL + ["is_new_device"]], columns=CATEGORICAL, drop_first=True)
    y = df["is_fraud"]

    # 3. Robust Scaling
    # Ensure no infinite values or NaNs before scaling
    X = X.replace([np.inf, -np.inf], np.nan).fillna(0)
    
    scaler = StandardScaler()
    # Only scale the numeric columns
    X[NUMERIC] = scaler.fit_transform(X[NUMERIC])

    # 4. Training with 'liblinear' (more stable for these warnings)
    model = LogisticRegression(class_weight="balanced", solver="liblinear", max_iter=1000)
    model.fit(X, y)

    # 5. Exporting Artifacts
    artifact = {
        "model_type": "logistic_regression",
        "intercept": float(model.intercept_[0]),
        "coefficients": {k: float(v) for k, v in zip(X.columns, model.coef_[0])},
        "scaler": {
            "mean": {k: float(v) for k, v in zip(NUMERIC, scaler.mean_)},
            "std": {k: float(v) for k, v in zip(NUMERIC, scaler.scale_)}
        },
        "feature_order": list(X.columns)
    }

    with open(MODEL_FILE, "w") as f:
        json.dump(artifact, f, indent=4)
    print(f"Success! Model artifact saved to {MODEL_FILE}")

if __name__ == "__main__":
    train()