import mysql.connector
import uuid
import random
import sys

from decimal import Decimal
from datetime import datetime, timedelta, timezone

# =========================================================
# DATABASE CONFIG
# =========================================================

DB_CONFIG = {
    "host": "localhost",
    "port": 3307,
    "user": "root",
    "password": "<your pass word>",
    "database": "gbank_Transactions"
}

TABLE_NAME = "transactions"

# =========================================================
# USERS
# =========================================================

USERS = {

    "legit": {
        "username": "user2",
        "email": "user2@gmail.com",
        "user_id": uuid.UUID("787c8a73-5674-42aa-a84d-5ba32f894e66"),
        "source_account": "ACC-IN-000123",
        "device_id": "DEV-ANDROID-MOBILE",
        "country": "IN"
    },

    "daily_limit": {
        "username": "user6",
        "email": "user6@gmail.com",
        "user_id": uuid.UUID("d68031d7-285a-41e1-b1f5-7ca16cdd3e45"),
        "source_account": "ACC-IN-000124",
        "device_id": "DEV-ANDROID-MOBILE",
        "country": "IN"
    },

    "velocity": {
        "username": "user4",
        "email": "user4@gmail.com",
        "user_id": uuid.UUID("a36dd4e5-919e-4660-b48d-22be5aa371b5"),
        "source_account": "ACC-IN-000125",
        "device_id": "DEV-ANDROID-MOBILE",
        "country": "IN"
    },

    "compound": {
        "username": "user8",
        "email": "user8@gmail.com",
        "user_id": uuid.UUID("f0ee26bc-6d10-41f7-8a4e-c18004c47325"),
        "source_account": "ACC-IN-000126",
        "device_id": "DEV-ANDROID-MOBILE",
        "country": "IN"
    }
}

# =========================================================
# MYSQL CONNECTION
# =========================================================

conn = mysql.connector.connect(**DB_CONFIG)
cursor = conn.cursor()

# =========================================================
# INSERT FUNCTION
# =========================================================

def insert_transaction(
        user,
        amount,
        txn_time,
        total_snapshot,
        txn_count_snapshot
):

    query = f"""
    INSERT INTO {TABLE_NAME} (
        amount,
        channel,
        country,
        created_at,
        device_id,
        email,
        idempotency_key,
        internal_status,
        source_account,
        target_account,
        total_amount_24h_snapshot,
        transaction_id,
        transaction_status,
        transaction_time,
        transaction_type,
        txn_count_24h_snapshot,
        updated_at,
        user_id,
        user_name
    )
    VALUES (
        %s, %s, %s, %s, %s, %s, %s, %s,
        %s, %s, %s, %s, %s, %s, %s, %s,
        %s, %s, %s
    )
    """

    values = (
        Decimal(amount),
        "UPI",
        user["country"],
        txn_time,
        user["device_id"],
        user["email"],
        str(uuid.uuid4()),
        "COMPLETED",
        user["source_account"],
        f"ACC-IN-{random.randint(100000,999999)}",
        Decimal(total_snapshot),
        str(uuid.uuid4()),
        "APPROVED",
        txn_time,
        "TRANSFER",
        txn_count_snapshot,
        txn_time,

        # IMPORTANT FIX
        user["user_id"].bytes,

        user["username"]
    )

    cursor.execute(query, values)

# =========================================================
# LEGIT SCENARIO
# =========================================================

def seed_legit():

    user = USERS["legit"]

    now = datetime.now(timezone.utc)

    running_total = Decimal("0.00")
    txn_count = 0

    for days_back in [6, 5, 4, 3, 2, 1]:

        amount = Decimal(random.randint(500, 3000))

        txn_time = now - timedelta(days=days_back)

        running_total += amount
        txn_count += 1

        insert_transaction(
            user,
            amount,
            txn_time,
            running_total,
            txn_count
        )

    conn.commit()

    print("LEGIT SCENARIO SEEDED")

# =========================================================
# DAILY LIMIT SCENARIO
# =========================================================

def seed_daily_limit():

    user = USERS["daily_limit"]

    now = datetime.now(timezone.utc)

    amounts = [20000, 25000, 30000, 20000]

    running_total = Decimal("0.00")
    txn_count = 0

    for hours_back, amount in zip([20, 15, 10, 5], amounts):

        txn_time = now - timedelta(hours=hours_back)

        running_total += Decimal(amount)
        txn_count += 1

        insert_transaction(
            user,
            amount,
            txn_time,
            running_total,
            txn_count
        )

    conn.commit()

    print("DAILY LIMIT SCENARIO SEEDED")

# =========================================================
# VELOCITY SCENARIO
# =========================================================

def seed_velocity():

    user = USERS["velocity"]

    now = datetime.now(timezone.utc)

    running_total = Decimal("0.00")
    txn_count = 0

    for i in range(25):

        txn_time = now - timedelta(minutes=(30 - i))

        amount = Decimal(random.randint(1000, 4000))

        running_total += amount
        txn_count += 1

        insert_transaction(
            user,
            amount,
            txn_time,
            running_total,
            txn_count
        )

    conn.commit()

    print("VELOCITY SCENARIO SEEDED")

# =========================================================
# COMPOUND FRAUD SCENARIO
# =========================================================

def seed_compound():

    user = USERS["compound"]

    now = datetime.now(timezone.utc)

    running_total = Decimal("0.00")
    txn_count = 0

    for days_back in [5, 4, 3, 2, 1]:

        txn_time = (
            now - timedelta(days=days_back)
        ).replace(
            hour=10,
            minute=0,
            second=0,
            microsecond=0
        )

        amount = Decimal(random.randint(1000, 4000))

        running_total += amount
        txn_count += 1

        insert_transaction(
            user,
            amount,
            txn_time,
            running_total,
            txn_count
        )

    conn.commit()

    print("COMPOUND SCENARIO SEEDED")

# =========================================================
# MAIN
# =========================================================

if len(sys.argv) != 2:
    print("Usage:")
    print("python3 user-history-seeder.py legit")
    print("python3 user-history-seeder.py daily_limit")
    print("python3 user-history-seeder.py velocity")
    print("python3 user-history-seeder.py compound")
    sys.exit(1)

scenario = sys.argv[1]

if scenario == "legit":
    seed_legit()

elif scenario == "daily_limit":
    seed_daily_limit()

elif scenario == "velocity":
    seed_velocity()

elif scenario == "compound":
    seed_compound()

else:
    print("INVALID SCENARIO")

cursor.close()
conn.close()