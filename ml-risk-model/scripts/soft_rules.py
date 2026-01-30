from datetime import datetime, timezone

HIGH_RISK_GEO = {"SG,HK"}

def calculate_soft_risk_score(tx):
    """
    Calculates the total soft risk score based on heuristic rules.
    Matches the Java SoftRule implementations exactly.
    """
    score = 0
    reasons = []

    # 1. AmountRiskRule
    amount = tx.get("amount", 0)
    if amount >= 100_000:
        score += 40
        reasons.append("HIGH_AMOUNT")
    elif amount >= 25_000:
        score += 20
        reasons.append("HIGH_AMOUNT")

    # 2. ChannelRiskRule
    if tx.get("channel") == "CARD":
        score += 15
        reasons.append("RISKY_CHANNEL")

    # 3. DailyAmountVelocityRiskRule
    daily_total = tx.get("total_amount_last_24h", 0)
    if daily_total >= 500_000:
        score += 50
        reasons.append("DAILY_AMOUNT_SPIKE")
    elif daily_total >= 200_000:
        score += 25
        reasons.append("DAILY_AMOUNT_SPIKE")

    # 4. GeoRiskRule
    if tx.get("country") in HIGH_RISK_GEO:
        score += 20
        reasons.append("GEO_RISK")

    # 5. NewDeviceRiskRule (Logic moved from ctx.isNewDevice())
    # Assuming the logic we discussed: null or 'UNKNOWN' means new
    device_id = tx.get("device_id")
    if device_id is None or device_id == "UNKNOWN" or device_id.startswith("NEW_"):
        score += 20
        reasons.append("NEW_DEVICE")

    # 6. TimeOfDayRiskRule (00:00 to 04:59)
    txn_time = tx.get("transaction_time", datetime.now(timezone.utc))
    hour = txn_time.hour
    if 0 <= hour <= 4:
        score += 10
        reasons.append("ODD_HOUR_TRANSACTION")

    # 7. TransactionVelocityRiskRule
    txn_count = tx.get("txn_count_last_24h", 0)
    if txn_count >= 20:
        score += 40
        reasons.append("HIGH_TXN_VELOCITY")
    elif txn_count >= 10:
        score += 20
        reasons.append("HIGH_TXN_VELOCITY")

    # 8. WithdrawalRiskRule
    if tx.get("transaction_type") == "WITHDRAWAL":
        score += 15
        reasons.append("WITHDRAWAL_RISK")

    return score, reasons