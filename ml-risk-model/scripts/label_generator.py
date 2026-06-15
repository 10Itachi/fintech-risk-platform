# ==========================================
# LABEL GENERATOR (FINAL - REALISTIC)
# ==========================================

import pandas as pd
import random
from pathlib import Path

# ==========================================
# FILE PATHS
# ==========================================
PROJECT_ROOT = Path(__file__).parent.parent

INPUT_FILE = PROJECT_ROOT / "artifacts" / "feature_dataset.csv"
OUTPUT_FILE = PROJECT_ROOT / "artifacts" / "final_dataset.csv"

# ==========================================
# CONFIG (CRITICAL)
# ==========================================

BASE_FRAUD_RATE = 0.03   # 3% baseline fraud
GLOBAL_SCALE = 0.4       # controls overall probability

# ==========================================
# FRAUD PROBABILITY FUNCTION
# ==========================================

def compute_fraud_probability(row):
    """
    Generates realistic fraud probability.

    Key:
    - Low base rate
    - Weighted signals
    - Controlled scaling
    """

    score = 0.0

    # ---------------- AMOUNT ----------------
    if row["amount"] > 20000:
        score += 0.08
    elif row["amount"] > 10000:
        score += 0.04

    # ---------------- VELOCITY ----------------
    if row["txn_count_last_24h"] > 15:
        score += 0.08
    elif row["txn_count_last_24h"] > 8:
        score += 0.04

    if row["total_amount_last_24h"] > 80000:
        score += 0.08

    # ---------------- DEVICE ----------------
    if row["is_new_device"] == 1:
        score += 0.10

    # ---------------- TIME ----------------
    if row["is_odd_hour"] == 1:
        score += 0.05

    # ---------------- GEO ----------------
    if row["country"] in ["SG", "HK"]:
        score += 0.06

    # ---------------- RANDOM NOISE ----------------
    score += random.uniform(0.0, 0.03)

    # ---------------- SCALE DOWN ----------------
    score = score * GLOBAL_SCALE

    # ---------------- ADD BASE RATE ----------------
    prob = BASE_FRAUD_RATE + score

    # ---------------- SOFT CAP ----------------
    # prevents extreme values
    prob = min(prob, 0.85)

    return prob


# ==========================================
# LABEL ASSIGNMENT
# ==========================================

def assign_labels(df):

    probabilities = []
    labels = []

    for _, row in df.iterrows():

        prob = compute_fraud_probability(row)

        probabilities.append(prob)

        # Bernoulli sampling
        label = 1 if random.random() < prob else 0

        labels.append(label)

    df["fraud_probability"] = probabilities
    df["is_fraud"] = labels

    return df


# ==========================================
# MAIN PIPELINE
# ==========================================

def run():
    print("Loading feature dataset...")

    df = pd.read_csv(INPUT_FILE)
    print(f"Loaded {len(df)} records")

    df = assign_labels(df)

    # Debug: fraud rate
    fraud_rate = df["is_fraud"].mean() * 100
    print(f"Fraud Rate: {fraud_rate:.2f}%")

    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(OUTPUT_FILE, index=False)

    print("Label generation completed")
    print(f"Saved to {OUTPUT_FILE}")


# ==========================================
# ENTRY POINT
# ==========================================

if __name__ == "__main__":
    run()