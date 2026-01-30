import pandas as pd
from pathlib import Path

DATA_FILE = Path(__file__).resolve().parent.parent / "artifacts" / "transactions.csv"

def run_analysis():
    df = pd.read_csv(DATA_FILE)
    
    print("--- DATASET QUALITY REPORT ---")
    print(f"Total Rows: {len(df)}")
    
    # Accuracy Parameter 1: Fraud Distribution
    rate = (df['is_fraud'].mean() * 100)
    print(f"Fraud Rate: {rate:.2f}% (Ideal: 2-10%)")

    # Accuracy Parameter 2: Score Correlation (Crucial)
    # The 'Fraud' group must have a significantly higher mean score than 'Legit'
    avg_fraud_score = df[df['is_fraud'] == 1]['soft_risk_score'].mean()
    avg_legit_score = df[df['is_fraud'] == 0]['soft_risk_score'].mean()
    
    print(f"Avg Score (Fraud): {avg_fraud_score:.2f}")
    print(f"Avg Score (Legit): {avg_legit_score:.2f}")
    
    if avg_fraud_score > (avg_legit_score * 2):
        print("STATUS: DATA ACCURATE (Clear signal detected)")
    else:
        print("STATUS: DATA WEAK (Scores are too similar)")

if __name__ == "__main__":
    run_analysis()