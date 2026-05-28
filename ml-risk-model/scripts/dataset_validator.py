# ==========================================
# DATASET VALIDATOR (PRODUCTION-STYLE)
# ==========================================

"""
PURPOSE:
--------
Validate dataset quality before training.

Checks:
-------
1. Fraud rate (class balance)
2. Feature separation (signal strength)
3. Distribution sanity
4. Correlation indicators

INPUT:
------
artifacts/final_dataset.csv

OUTPUT:
-------
Console report
"""

import pandas as pd
from pathlib import Path

# ==========================================
# FILE PATH
# ==========================================
PROJECT_ROOT = Path(__file__).parent.parent
DATA_FILE = PROJECT_ROOT/ "artifacts" / "final_dataset.csv"

# ==========================================
# VALIDATION FUNCTIONS
# ==========================================

def check_fraud_rate(df):
    """
    Checks if fraud distribution is realistic.
    """

    fraud_rate = df["is_fraud"].mean() * 100

    print(f"\n[Fraud Rate] {fraud_rate:.2f}%")

    if 2 <= fraud_rate <= 15:
        print("✔ Fraud rate is realistic")
    else:
        print("⚠ Fraud rate is unrealistic (ideal: 2–15%)")


def check_feature_separation(df):
    """
    Checks whether fraud and non-fraud are distinguishable.
    """

    print("\n[Feature Separation]")

    features = [
        "amount",
        "txn_count_last_24h",
        "total_amount_last_24h",
        "fraud_probability"
    ]

    for feature in features:

        fraud_mean = df[df["is_fraud"] == 1][feature].mean()
        legit_mean = df[df["is_fraud"] == 0][feature].mean()

        print(f"{feature}: fraud={fraud_mean:.2f}, legit={legit_mean:.2f}")

        if fraud_mean > legit_mean:
            print(f"✔ {feature} shows signal")
        else:
            print(f"⚠ {feature} weak signal")


def check_class_balance(df):
    """
    Checks number of fraud vs legit samples.
    """

    counts = df["is_fraud"].value_counts()

    print("\n[Class Distribution]")
    print(counts)

    if len(counts) < 2:
        print("❌ Only one class present → model useless")
    else:
        print("✔ Both classes present")


def check_nulls(df):
    """
    Ensure no missing values.
    """

    nulls = df.isnull().sum().sum()

    print(f"\n[Missing Values] {nulls}")

    if nulls == 0:
        print("✔ No missing values")
    else:
        print("⚠ Missing values present")


def check_basic_stats(df):
    """
    Prints basic statistics.
    """

    print("\n[Basic Statistics]")
    print(df.describe())


# ==========================================
# MAIN PIPELINE
# ==========================================

def run():
    print("Loading dataset...")

    df = pd.read_csv(DATA_FILE)

    print(f"Loaded {len(df)} records")

    check_fraud_rate(df)
    check_class_balance(df)
    check_feature_separation(df)
    check_nulls(df)
    check_basic_stats(df)

    print("\nDataset validation completed")


# ==========================================
# ENTRY POINT
# ==========================================

if __name__ == "__main__":
    run()