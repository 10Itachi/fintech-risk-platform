import random, uuid, csv
from datetime import datetime, timezone, timedelta
from pathlib import Path
from hard_rules import violates_hard_rules
from soft_rules import calculate_soft_risk_score

BASE_DIR = Path(__file__).resolve().parent.parent
OUTPUT_FILE = BASE_DIR / "artifacts" / "transactions.csv"

def generate_dataset(n=50000):
    rows = []
    print(f"Generating {n} records with improved logic...")

    for _ in range(n):
        # IMPROVEMENT 1: Bias the random values to create more "Normal" users
        # Most users spend small amounts and stay under limits
        is_high_risk_user = random.random() < 0.10 # Only 10% of users are "risky"
        
        tx = {
            "transaction_id": str(uuid.uuid4()),
            "user_id": random.randint(1000, 9999),
            "amount": round(random.uniform(10, 45000) if is_high_risk_user else random.uniform(10, 5000), 2),
            "channel": random.choice(["UPI", "CARD", "NET_BANKING"]),
            "transaction_type": random.choice(["DEPOSIT", "TRANSFER", "WITHDRAWAL"]),
            "source_account": f"ACC-{random.randint(100, 200)}",
            "target_account": f"ACC-{random.randint(201, 300)}",
            "country": random.choice(["IN", "US", "SG", "HK"]), 
            "device_id": random.choice(["DEV_X", "DEV_Y", "NEW_DEV_Z"]) if is_high_risk_user else "DEV_REGULAR",
            "transaction_time": datetime.now(timezone.utc),
            "total_amount_last_24h": round(random.uniform(0, 150000) if is_high_risk_user else random.uniform(0, 10000), 2),
            "txn_count_last_24h": random.randint(5, 25) if is_high_risk_user else random.randint(1, 5)
        }

        if violates_hard_rules(tx):
            continue

        score, _ = calculate_soft_risk_score(tx)
        
        # IMPROVEMENT 2: Sharpen the Fraud Probability
        # We use a power function so that low scores almost NEVER become fraud
        # and only very high scores (90+) have a high chance.
        fraud_prob = (score / 180.0) ** 3  # Exponential growth for sharper separation
        
        if random.random() < fraud_prob:
            tx["transaction_status"] = "DECLINED"
            tx["is_fraud"] = 1
        else:
            tx["transaction_status"] = "APPROVED"
            tx["is_fraud"] = 0
        
        tx["soft_risk_score"] = score
        rows.append(tx)

    # Save to CSV
    Path(OUTPUT_FILE.parent).mkdir(parents=True, exist_ok=True)
    with open(OUTPUT_FILE, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=rows[0].keys())
        writer.writeheader()
        writer.writerows(rows)
    print(f"Success! Saved {len(rows)} valid transactions to {OUTPUT_FILE}")

if __name__ == "__main__":
    generate_dataset()