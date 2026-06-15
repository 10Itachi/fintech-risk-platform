# ==========================================
# MODEL TRAINING SCRIPT (FINAL - STABLE + JAVA SAFE)
# ==========================================

import pandas as pd
import numpy as np
from model_exporter import export_model
from pathlib import Path

from sklearn.model_selection import train_test_split ## to avoid overfitting
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LogisticRegression

from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    roc_auc_score,
    confusion_matrix
)

# ==========================================
# FILE PATH
# ==========================================
PROJECT_ROOT = Path(__file__).parent.parent
DATA_FILE = PROJECT_ROOT / "artifacts/final_dataset.csv"

# ==========================================
# FEATURE CONFIG
# ==========================================
NUMERIC_FEATURES = ["amount", "total_amount_last_24h", "txn_count_last_24h"]
CATEGORICAL_FEATURES = ["channel", "transaction_type", "country"]
BINARY_FEATURES = ["is_new_device", "is_odd_hour"]
TARGET = "is_fraud"

# ==========================================
# DATA PREPARATION
# ==========================================

def prepare_features(df):
    X = df[NUMERIC_FEATURES + CATEGORICAL_FEATURES + BINARY_FEATURES].copy()
    y = df[TARGET]

    # 1. Handle missing
    X = X.fillna(0)

    # 2. LOG TRANSFORM (CRITICAL)
    for col in ["amount", "total_amount_last_24h"]:
        X[col] = np.log1p(X[col].clip(lower=0))

    # 3. One-hot encoding
    X = pd.get_dummies(X, columns=CATEGORICAL_FEATURES)

    # 4. FINAL NUMERIC SAFETY
    X = X.replace([np.inf, -np.inf], 0)
    X = X.fillna(0)
    X = X.astype(float)

    # 5. REMOVE ZERO VARIANCE COLUMNS (CRITICAL)
    X = X.loc[:, X.std() > 0]

    return X, y


# ==========================================
# TRAINING FUNCTION
# ==========================================

def train_model(X_train, y_train):

    scaler = StandardScaler()

    numeric_cols = NUMERIC_FEATURES
    other_cols = [c for c in X_train.columns if c not in numeric_cols]

    # Scale numeric
    X_train_numeric = scaler.fit_transform(X_train[numeric_cols])

    # Light clipping
    X_train_numeric = np.clip(X_train_numeric, -5, 5)

    # Categorical untouched
    X_train_other = X_train[other_cols].values

    # Combine
    X_train_final = np.hstack([X_train_numeric, X_train_other])

    # FINAL SAFETY GUARD (CRITICAL)
    X_train_final = np.nan_to_num(
        X_train_final,
        nan=0.0,
        posinf=5.0,
        neginf=-5.0
    )

    feature_order = numeric_cols + other_cols

    model = LogisticRegression(
        class_weight="balanced",
        solver="lbfgs",
        max_iter=1000,
        C=0.05   # stronger regularization
    )

    model.fit(X_train_final, y_train)

    return model, scaler, other_cols, feature_order


# ==========================================
# EVALUATION FUNCTION
# ==========================================

def evaluate_model(model, scaler, other_cols, X_test, y_test):

    numeric_cols = NUMERIC_FEATURES

    # Scale numeric
    X_test_numeric = scaler.transform(X_test[numeric_cols])
    X_test_numeric = np.clip(X_test_numeric, -5, 5)

    # Categorical
    X_test_other = X_test[other_cols].values

    # Combine
    X_test_final = np.hstack([X_test_numeric, X_test_other])

    # FINAL SAFETY GUARD
    X_test_final = np.nan_to_num(
        X_test_final,
        nan=0.0,
        posinf=5.0,
        neginf=-5.0
    )

    y_prob = model.predict_proba(X_test_final)[:, 1]

    print("\n=== PROBABILITY STATS ===")
    print(f"Min: {y_prob.min()}")
    print(f"Max: {y_prob.max()}")
    print(f"Mean: {y_prob.mean()}")

    y_pred = model.predict(X_test_final)

    print("\n=== MODEL EVALUATION ===")
    print(f"Accuracy:  {accuracy_score(y_test, y_pred):.4f}")
    print(f"Precision: {precision_score(y_test, y_pred):.4f}")
    print(f"Recall:    {recall_score(y_test, y_pred):.4f}")
    print(f"ROC-AUC:   {roc_auc_score(y_test, y_prob):.4f}")

    print("\nConfusion Matrix:")
    print(confusion_matrix(y_test, y_pred))


# ==========================================
# MAIN PIPELINE
# ==========================================

def run():
    if not DATA_FILE.exists():
        print(f"Error: {DATA_FILE} not found!")
        return

    print(f"Loading dataset from {DATA_FILE}...")
    df = pd.read_csv(DATA_FILE)

    X, y = prepare_features(df)

    print("Splitting dataset...")
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    print("Training model...")
    model, scaler, other_cols, feature_order = train_model(X_train, y_train)

    evaluate_model(model, scaler, other_cols, X_test, y_test)

    export_model(model, scaler, feature_order)

    print("\nModel exported successfully to artifacts/logistic_model.json")


# ==========================================
# ENTRY POINT
# ==========================================

if __name__ == "__main__":
    run()