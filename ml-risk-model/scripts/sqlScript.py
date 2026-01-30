import csv
import mysql.connector
from datetime import datetime

# ================= DB CONFIG =================

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "Emma10watson*",
    "database": "gbank_Transactions",
    "autocommit": False
}

USERS_CSV_PATH = "/Users/sangmeshpatil/Study/final project/ml-risk-model/artifacts/users.csv"
TXNS_CSV_PATH  = "/Users/sangmeshpatil/Study/final project/ml-risk-model/artifacts/history_transactions_set.csv"

# ================= CONNECT =================

conn = mysql.connector.connect(**DB_CONFIG)
cursor = conn.cursor()

try:
    print("Connected to MySQL")

    # ================= LOAD USERS =================

    user_insert_sql = """
INSERT INTO users
(user_id, user_name, phone_number, password_hash, email, role, is_active, created_at)
VALUES (%s,%s,%s,%s,%s,%s,%s,%s)
"""


    with open(USERS_CSV_PATH, newline="") as f:
        reader = csv.DictReader(f)
        user_rows = []

        for row in reader:
            user_rows.append((
                int(row["userId"]),
                row["userName"],
                row["phoneNumber"],
                row["passwordHash"],
                row["email"],
                row["role"],
                row["isActive"],
                row["createdAt"].replace("T", " ")
            ))

    cursor.executemany(user_insert_sql, user_rows)
    print(f"Inserted {len(user_rows)} users")

    # ================= LOAD TRANSACTIONS =================

    txn_insert_sql = """
INSERT INTO transactions
(transaction_id, user_id, amount, status, risk_score, channel,
 transaction_type, source_account, target_account,
 country, device_id, created_at, reason_code, fraud_probability)
VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
"""


    with open(TXNS_CSV_PATH, newline="") as f:
        reader = csv.DictReader(f)
        txn_rows = []

        for row in reader:
            txn_rows.append((
                row["transactionId"],
                int(row["userId"]),
                float(row["amount"]),
                row["status"],
                int(row["riskScore"]),
                row["channel"],
                row["transactionType"],
                row["sourceAccount"],
                row["targetAccount"],
                row["country"],
                row["deviceId"],
                row["createdAt"].replace("T", " "),
                row["reasonCode"] if row["reasonCode"] else None,
                float(row["fraudProbability"])
            ))

    cursor.executemany(txn_insert_sql, txn_rows)
    print(f"Inserted {len(txn_rows)} transactions")

    # ================= COMMIT =================

    conn.commit()
    print("Data successfully committed")

except Exception as e:
    conn.rollback()
    print("ERROR — rollback executed")
    print(e)

finally:
    cursor.close()
    conn.close()
    print("Connection closed")


# DELETE FROM users
# WHERE user_id <> 1;

#TRUNCATE TABLE transaction_history;
