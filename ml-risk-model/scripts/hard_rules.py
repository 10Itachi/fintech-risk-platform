from datetime import datetime, timezone, timedelta

# Configuration mimicking RiskPolicyProperties
POLICY = {
    "max_single_amount": 50000,
    "max_daily_amount": 100000,
    "blocked_countries": {"PK", "AF", "BD"},
    "card_withdrawal_limit": 50000,
    "max_future_drift_minutes": 5,   # matches props.getMaxFutureDrift()
    "max_past_drift_days": 30        # matches props.getMaxPastDrift()
}

def violates_hard_rules(tx):
    """
    Returns True if any hard rule is triggered (REJECT), 
    False if the transaction passes to the next stage.
    """
    # 1. InvalidAmountRule
    if tx["amount"] <= 0:
        print("Triggered: INVALID_AMOUNT")
        return True

    # 2. AmountLimitRule
    if tx["amount"] > POLICY["max_single_amount"]:
        print("Triggered: AMOUNT_LIMIT_EXCEEDED")
        return True

    # 3. DailyTotalAmountLimitRule
    if tx.get("total_amount_last_24h", 0) > POLICY["max_daily_amount"]:
        print("Triggered: DAILY_AMOUNT_LIMIT_EXCEEDED")
        return True

    # 4. BlockedCountryRule
    if tx["country"] in POLICY["blocked_countries"]:
        print("Triggered: BLOCKED_COUNTRY")
        return True

    # 5. SelfTransferRule
    if tx["transaction_type"] == "TRANSFER" and tx["source_account"] == tx["target_account"]:
        print("Triggered: SELF_TRANSFER_NOT_ALLOWED")
        return True

    # 6. CardWithdrawalLimitRule
    if (tx["channel"] == "CARD" and 
        tx["transaction_type"] == "WITHDRAWAL" and 
        tx["amount"] >= POLICY["card_withdrawal_limit"]):
        print("Triggered: CARD_WITHDRAWAL_LIMIT_EXCEEDED")
        return True

    # 7. UnsupportedChannelForTransactionRule
    if tx["channel"] == "CARD" and tx["transaction_type"] == "SELF":
        print("Triggered: UNSUPPORTED_CHANNEL_FOR_TRANSACTION")
        return True
    if tx["channel"] == "UPI" and tx["transaction_type"] == "WITHDRAWAL":
        print("Triggered: UNSUPPORTED_CHANNEL_FOR_TRANSACTION")
        return True

    # 8. InvalidTransactionTimeRule
    # Note: Use timezone-aware comparison to match Java's Instant.now()
    now = datetime.now(timezone.utc)
    txn_time = tx["transaction_time"] # Expected to be a datetime object

    future_limit = now + timedelta(minutes=POLICY["max_future_drift_minutes"])
    past_limit = now - timedelta(days=POLICY["max_past_drift_days"])

    if txn_time > future_limit or txn_time < past_limit:
        print("Triggered: INVALID_TRANSACTION_TIME")
        return True

    return False