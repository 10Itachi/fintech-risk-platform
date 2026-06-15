# ==========================================
# FEATURE ENGINEERING SCRIPT (FINAL)
# ==========================================

"""
PURPOSE:
--------
This script transforms raw transaction data into ML-ready features.

CRITICAL DESIGN:
---------------
- Ensures feature parity with Java (MlFeatureBuilder)
- Simulates DB-based device history (is_new_device)
- Does NOT include any ML logic or scoring
- Pure transformation layer

INPUT:
------
artifacts/raw_transactions.csv

OUTPUT:
-------
artifacts/feature_dataset.csv
"""


import pandas as pd #Pandas is the most important library in Python for data. We alias it as pd to save typing. It’s like a "Programmable Excel" or a List<Map<String, Object>> on steroids.
from pathlib import Path

# ==========================================
# FILE PATHS
# ==========================================

PROJECT_ROOT = Path(__file__).parent.parent #This uses the pathlib library to find your main folder. __file__ refers to the current script.

INPUT_FILE = PROJECT_ROOT / "artifacts" / "raw_transactions.csv"
OUTPUT_FILE = PROJECT_ROOT / "artifacts" / "feature_dataset.csv"


# ==========================================
# DEVICE FEATURE (DB-SIMULATED)
# ==========================================

def compute_device_feature(df):
    """
    Simulates DB-backed device history.

    For each user:
    - First time seeing a device → is_new_device = 1
    - Seen before → is_new_device = 0

    This mimics Java logic:
    DeviceHistoryService → DB lookup

    WHY:
    ----
    Training must match inference behavior exactly.
    """

    seen_devices = {}  # user_id → set(device_ids) #This creates an empty Dictionary. 
    #In Java, this is exactly like a HashMap<Integer, Set<String>>
    is_new_device_list = []

    for _, row in df.iterrows():

        user = row["user_id"]
        device = row["device_id"]

        # Initialize user bucket
        if user not in seen_devices:
            seen_devices[user] = set()

        # Check if device seen before
        if device in seen_devices[user]:
            is_new_device_list.append(0)  # known device
        else:
            is_new_device_list.append(1)  # new device
            seen_devices[user].add(device)

    return is_new_device_list


# ==========================================
# FEATURE BUILDING LOGIC
# ==========================================

def build_features(df):
    """
    Converts raw transaction data into ML features.

    IMPORTANT:
    ----------
    These features MUST match Java exactly.
    """

    # ---------------- SORTING (CRITICAL) ----------------
    # Ensures correct time-based device tracking
    df = df.sort_values(by=["user_id", "transaction_time"])

    # ---------------- NUMERIC FEATURES ----------------
    # Already generated from simulator

    df["amount"] = df["amount"]
    df["total_amount_last_24h"] = df["total_amount_last_24h"]
    df["txn_count_last_24h"] = df["txn_count_last_24h"]

    # ---------------- DEVICE FEATURE ----------------
    df["is_new_device"] = compute_device_feature(df)

    # ---------------- TIME FEATURE ----------------

    df["transaction_time"] = pd.to_datetime(df["transaction_time"])

    df["hour_of_day"] = df["transaction_time"].dt.hour

    # Match Java rule: 0–4 → risky hours
    df["is_odd_hour"] = df["hour_of_day"].apply(
        lambda h: 1 if 0 <= h <= 4 else 0
    )

    # ---------------- CATEGORICAL FEATURES ----------------
    # Must match Java enums exactly

    df["channel"] = df["channel"]
    df["transaction_type"] = df["transaction_type"]
    df["country"] = df["country"]

    # ==========================================
    # FINAL FEATURE SET
    # ==========================================

    feature_columns = [
        "amount",
        "total_amount_last_24h",
        "txn_count_last_24h",
        "is_new_device",
        "is_odd_hour",
        "channel",
        "transaction_type",
        "country"
    ]

    return df[feature_columns]


# ==========================================
# MAIN PIPELINE
# ==========================================

def run():
    print("Loading raw transactions...")

    df = pd.read_csv(INPUT_FILE)

    print(f"Loaded {len(df)} records")

    feature_df = build_features(df)

    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)

    feature_df.to_csv(OUTPUT_FILE, index=False)

    print("Feature engineering completed")
    print(f"Saved to {OUTPUT_FILE}")


# ==========================================
# ENTRY POINT
# ==========================================

if __name__ == "__main__":
    run()