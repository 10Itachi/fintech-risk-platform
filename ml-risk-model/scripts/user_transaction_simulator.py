# ==========================================
# USER + TRANSACTION SIMULATOR
# ==========================================

import csv
import uuid
import random
from datetime import datetime, timedelta
from pathlib import Path
from collections import deque

# ==========================================
# CONFIGURATION
# ==========================================

NUM_USERS = 100
MAX_TXNS_PER_USER = 40

CHANNELS = ["UPI", "CARD", "NET_BANKING"]
TX_TYPES = ["TRANSFER", "WITHDRAWAL", "DEPOSIT"]
COUNTRIES = ["IN", "US", "SG", "AE"]

# Path(__file__).parent is the 'scripts' folder
# .parent again goes up to 'ml-risk-model'
PROJECT_ROOT = Path(__file__).parent.parent 

OUTPUT_FILE = PROJECT_ROOT / "artifacts" / "raw_transactions.csv"

# ==========================================
# USER PROFILE GENERATION
# ==========================================

def generate_users(n):
    """
    Each user has a behavior profile:
    - avg_amount → typical spending
    - risk_appetite → how risky they are
    - preferred_channel
    """

    users = []

    for user_id in range(1, n + 1):

        profile = {
            "user_id": user_id,  #just the number int

            # Avg spending pattern
            "avg_amount": random.choice([2000, 5000, 10000, 20000]),

            # Risk level (important for fraud simulation)
            "risk_level": random.choice(["LOW", "MEDIUM", "HIGH"]),

            # Preferred channel
            "preferred_channel": random.choice(CHANNELS),

            # Base country
            "home_country": "IN"
        }

        users.append(profile)

    return users

# ==========================================
# TRANSACTION GENERATION
# ==========================================

def generate_transactions(users):
    """
    Generates transaction stream per user.
    Maintains 24h history using deque (IMPORTANT)
    """

    transactions = []

    for user in users:
        # stores (time, amount) A deque (double-ended queue) is like a tube. 
        #We add new transactions to the right and pop old ones out of the left. out<-- <-- IN
        # This is used to track the user's "Rolling 24-hour" history.
        last_24h = deque()

        txn_count = random.randint(15, MAX_TXNS_PER_USER)#transacrion per user

        #We set the "start time" for this user to be 48 hours ago so we have time to build up a history.
        current_time = datetime.utcnow() - timedelta(days=2)

        for _ in range(txn_count):

            # ---------------- TIME PROGRESSION ----------------

            # We "tick" the clock forward by a random amount (5 minutes to 2 hours). 
            # This ensures transactions aren't all happening at the exact same second.
            current_time += timedelta(minutes=random.randint(5, 120))

            # Remove old transactions beyond 24h window
            #This is the "Cleanup" logic. It checks the oldest transaction in our "tube" (last_24h[0]). 
            # If it happened more than 24 hours ago, it's no longer relevant for our 24h-velocity features.
            while last_24h and last_24h[0][0] < current_time - timedelta(hours=24):
                last_24h.popleft()

            # ---------------- AMOUNT GENERATION ----------------

            #We look at the user's profile to see if they usually spend 2,000 or 20,000.
            base = user["avg_amount"] 

            # Introduce variability
            #We pick a random amount. A user who usually spends 2,000 will now spend somewhere between 1,000 and 5,000. 
            # round(..., 2) makes it look like real money (e.g., 10.50).
            amount = round(random.uniform(base * 0.5, base * 2.5), 2)

            # High-risk users occasionally spike
            #If the user was labeled "HIGH RISK" in their profile, 
            # there is a 20% chance this specific transaction will bemuch larger than usual.
            #If the risk check passes, we multiply the amount by 2 to 4 times. 
            # This creates the "outlier" data that your ML model needs to learn to catch.
            if user["risk_level"] == "HIGH" and random.random() < 0.2:
                amount *= random.uniform(2, 4)

            # ---------------- DEVICE LOGIC ----------------

            # Simulate device switching
            #There's a 20% chance this transaction is on a new device.
            is_new_device = random.random() < 0.2

            #If it's a new device, we generate a random UUID (DEV-abc123). If not, we use a string like KNOWN-5.
            device_id = (
                f"DEV-{uuid.uuid4().hex[:6]}"
                if is_new_device
                else f"KNOWN-{user['user_id']}"
            )

            # ---------------- CHANNEL ----------------
            #70% of the time, the user uses their "preferred channel" (like UPI). 30% of the time, they try something else. 
            # This simulates real human inconsistency.
            channel = (
                user["preferred_channel"]
                if random.random() < 0.7
                else random.choice(CHANNELS)
            )

            # ---------------- COUNTRY ----------------

            country = (
                user["home_country"]
                if random.random() < 0.8
                else random.choice(COUNTRIES)
            )

            # ---------------- TRANSACTION ----------------

            tx = {
                "transaction_id": str(uuid.uuid4()),
                "user_id": user["user_id"],
                "amount": amount,
                "channel": channel,
                "transaction_type": random.choice(TX_TYPES),
                "country": country,
                "device_id": device_id,
                "transaction_time": current_time.isoformat(),
                "total_amount_last_24h": sum(a for _, a in last_24h), #This sums up all the amounts currently inside our "tube" (the transactions from the last 24 hours).
                "txn_count_last_24h": len(last_24h) #This counts how many transactions are currently in the tube.
            }

            # Add current transaction to history
            last_24h.append((current_time, amount))

            transactions.append(tx)

    return transactions

# ==========================================
# SAVE CSV
# ==========================================

def save_to_csv(data):

    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)

    with open(OUTPUT_FILE, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=data[0].keys())
        writer.writeheader()
        writer.writerows(data)


# MAIN PIPELINE

def run():
    users = generate_users(NUM_USERS)
    transactions = generate_transactions(users)
    save_to_csv(transactions)

    print(f"Generated {len(transactions)} transactions")
    print(f"Saved to {OUTPUT_FILE}")

if __name__ == "__main__":
    run()