import csv
import uuid
import random
import hashlib
from datetime import datetime, timedelta
from decimal import Decimal
from collections import deque

# =====================================================
# CONFIGURATION
# =====================================================

START_USER_ID = 2
NUM_USERS = 40
MAX_TXNS_PER_USER = 50

CHANNELS = ["UPI", "CARD", "NET_BANKING"]
TX_TYPES = ["TRANSFER", "WITHDRAWAL", "DEPOSIT"]
COUNTRIES = ["IN", "US", "SG", "AE"]

STATUS_APPROVED = "APPROVED"
STATUS_REVIEW = "REVIEW"
STATUS_DECLINED = "DECLINED"

ROLE_USER = "USER"
IS_ACTIVE_ACTIVE = 0    # 0 = ACTIVE
IS_ACTIVE_INACTIVE = 1  # 1 = INACTIVE

# =====================================================
# FRAUD PROBABILITY (SYNCED WITH JAVA)
# =====================================================

def fraud_probability(score: int) -> float:
    if score < 30:
        return 0.05
    elif score < 50:
        return 0.20
    elif score < 70:
        return 0.35
    elif score < 90:
        return 0.55
    elif score < 120:
        return 0.80
    else:
        return 0.95

# =====================================================
# HELPERS
# =====================================================

def hash_password(raw: str) -> str:
    return hashlib.sha256(raw.encode()).hexdigest()

def random_phone():
    return str(random.randint(6000000000, 9999999999))

def random_account():
    return f"ACC-IN-{random.randint(100000,999999)}"

def random_device(new_device):
    if new_device:
        return f"NEW-DEV-{uuid.uuid4().hex[:6]}"
    return f"DEV-IOS-{uuid.uuid4().hex[:6]}"

# =====================================================
# REASON CODES (LOCKED VOCABULARY)
# =====================================================

def derive_reason_codes(tx, txn_count_24h, amount_24h):
    reasons = []

    if tx["amount"] > 10000:
        reasons.append("HIGH_AMOUNT")

    if tx["channel"] == "CARD":
        reasons.append("HIGH_RISK_CHANNEL")

    if amount_24h >= 30000:
        reasons.append("HIGH_DAILY_TRANSACTION_VOLUME")

    if tx["country"] != "IN":
        reasons.append("FOREIGN_COUNTRY")

    if tx["deviceId"].startswith("NEW"):
        reasons.append("NEW_OR_UNSEEN_DEVICE")

    hour = tx["createdAt"].hour
    if hour < 6 or hour > 22:
        reasons.append("TIME_OF_DAY_RISK")

    if txn_count_24h >= 8:
        reasons.append("HIGH_TRANSACTION_FREQUENCY")

    if tx["transactionType"] == "WITHDRAWAL" and tx["amount"] >= 10000:
        reasons.append("HIGH_WITHDRAWAL_RISK")

    return reasons or None

# =====================================================
# EDGE CASE REGISTRY
# =====================================================

edge_cases = {}

def register_edge(user_id, edge_type, created_at, txn_count_24h, amount_24h):
    key = (user_id, edge_type)
    if key in edge_cases:
        return

    edge_cases[key] = {
        "userId": user_id,
        "edgeType": edge_type,
        "lastTxnTime": created_at.isoformat(),
        "txnCountLast24h": txn_count_24h,
        "totalAmountLast24h": round(amount_24h, 2)
    }

# =====================================================
# EDGE → NEXT TRANSACTION MAPPING
# =====================================================

EDGE_NEXT_TXN = {
    "HIGH_TRANSACTION_FREQUENCY": ("TRANSFER", 5000, "REVIEW"),
    "HIGH_DAILY_TRANSACTION_VOLUME": ("TRANSFER", 8000, "REVIEW"),
    "HIGH_AMOUNT": ("TRANSFER", 15000, "DECLINED"),
    "HIGH_RISK_CHANNEL": ("CARD", 7000, "REVIEW"),
    "FOREIGN_COUNTRY": ("TRANSFER", 6000, "REVIEW"),
    "NEW_OR_UNSEEN_DEVICE": ("TRANSFER", 7000, "REVIEW"),
    "TIME_OF_DAY_RISK": ("TRANSFER", 4000, "REVIEW"),
    "HIGH_WITHDRAWAL_RISK": ("WITHDRAWAL", 15000, "DECLINED"),
}

# =====================================================
# MAIN GENERATION
# =====================================================

users = []
transactions = []
user_phone_map = {}

for user_id in range(START_USER_ID, START_USER_ID + NUM_USERS):

    username = f"user_{user_id}"
    user_created = datetime.utcnow() - timedelta(days=random.randint(30, 365))
    phone = random_phone()

    # ---------------- USER ----------------
    users.append({
        "userId": user_id,
        "userName": username,
        "phoneNumber": phone,
        "passwordHash": hash_password("password"),
        "email": f"{username}@mail.com",
        "role": ROLE_USER,
        "isActive": IS_ACTIVE_ACTIVE,
        "createdAt": user_created.isoformat()
    })

    user_phone_map[user_id] = phone

    # ---------------- TRANSACTION HISTORY ----------------
    last_24h = deque()
    txn_count = random.randint(20, MAX_TXNS_PER_USER)
    start_time = datetime.utcnow() - timedelta(days=2)

    for _ in range(txn_count):

        created_at = start_time + timedelta(minutes=random.randint(5, 90))

        while last_24h and last_24h[0][0] < created_at - timedelta(hours=24):
            last_24h.popleft()

        amount = float(Decimal(random.uniform(200, 48000)).quantize(Decimal("0.01")))
        last_24h.append((created_at, amount))

        txn_count_24h = len(last_24h)
        amount_24h = sum(a for _, a in last_24h)

        tx = {
            "transactionId": str(uuid.uuid4()),
            "userId": user_id,
            "amount": amount,
            "channel": random.choice(CHANNELS),
            "transactionType": random.choice(TX_TYPES),
            "country": random.choice(COUNTRIES),
            "deviceId": random_device(random.random() < 0.25),
            "sourceAccount": random_account(),
            "targetAccount": random_account(),
            "createdAt": created_at
        }

        # ---------------- RISK SCORE ----------------
        score = 0
        if amount > 10000: score += 30
        if amount > 30000: score += 40
        if tx["country"] != "IN": score += 20
        if tx["deviceId"].startswith("NEW"): score += 15
        if txn_count_24h >= 8: score += 25
        if amount_24h >= 30000: score += 20

        fraudProb = fraud_probability(score)

        # ---------------- STATUS (JAVA POLICY) ----------------
        if fraudProb >= 0.80:
            status = STATUS_DECLINED
        elif fraudProb >= 0.40:
            status = STATUS_REVIEW
        else:
            status = STATUS_APPROVED

        reasons = derive_reason_codes(tx, txn_count_24h, amount_24h)

        if reasons:
            for r in reasons:
                register_edge(user_id, r, created_at, txn_count_24h, amount_24h)

        transactions.append({
            "transactionId": tx["transactionId"],
            "userId": user_id,
            "amount": amount,
            "status": status,
            "riskScore": score,
            "channel": tx["channel"],
            "transactionType": tx["transactionType"],
            "sourceAccount": tx["sourceAccount"],
            "targetAccount": tx["targetAccount"],
            "country": tx["country"],
            "deviceId": tx["deviceId"],
            "createdAt": created_at.isoformat(),
            "reasonCode": ",".join(reasons) if reasons else None,
            "fraudProbability": round(fraudProb, 4)
        })

# =====================================================
# WRITE CSV FILES
# =====================================================

with open("artifacts/users.csv", "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=users[0].keys())
    writer.writeheader()
    writer.writerows(users)

with open("artifacts/history_transactions_set.csv", "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=transactions[0].keys())
    writer.writeheader()
    writer.writerows(transactions)

edge_rows = []
for (_, edge_type), e in edge_cases.items():
    txn_type, amt, expected = EDGE_NEXT_TXN[edge_type]
    edge_rows.append({
        "userId": e["userId"],
        "phoneNumber": user_phone_map[e["userId"]],
        "edgeType": edge_type,
        "lastTxnTime": e["lastTxnTime"],
        "txnCountLast24h": e["txnCountLast24h"],
        "totalAmountLast24h": e["totalAmountLast24h"],
        "suggestedNextAmount": amt,
        "suggestedNextTxnType": txn_type,
        "expectedOutcome": expected
    })

with open("artifacts/edge_cases.csv", "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=edge_rows[0].keys())
    writer.writeheader()
    writer.writerows(edge_rows)

print(f"Generated {len(users)} users")
print(f"Generated {len(transactions)} transactions")
print(f"Generated {len(edge_rows)} edge case scenarios")
