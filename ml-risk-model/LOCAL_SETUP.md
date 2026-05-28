# LOCAL_SETUP.md

# Local Setup Guide - ML Risk Model

## Prerequisites

Required:

- Python 3.14+
- pip
- Docker (optional for containerized execution)

---

# Repository Structure

```text
ml-risk-model/
│
├── scripts/
├── artifacts/
├── output/
├── TransactionHistorySet/
├── run_pipeline.py
├── requirements.txt
└── Dockerfile
```

---

# Install Dependencies

## Local Python Setup

Create virtual environment:

```bash
python3 -m venv venv
```

Activate:

### macOS/Linux

```bash
source venv/bin/activate
```

Install dependencies:

```bash
pip install -r requirements.txt
```

---

# Run Pipeline Locally

Execute:

```bash
python run_pipeline.py
```

The pipeline automatically executes:

1. transaction simulation
2. feature engineering
3. fraud label generation
4. dataset validation
5. model training
6. model export

---

# Generated Outputs

## Datasets

Generated inside:

```text
artifacts/
```

## Production Model

Generated inside:

```text
output/
```

Main runtime artifact:

```text
output/active-model.json
```

---

# Docker Execution

## Build Image

```bash
docker build -t ml-risk-model .
```

## Run Container

```bash
docker run ml-risk-model
```

---

# Seeder Scripts

Seeder scripts inside:

```text
TransactionHistorySet/
```

are intended for:

- fraud scenario testing
- custom transaction history generation
- integration testing

These scripts are manually executed as needed.

Example:

```bash
python TransactionHistorySet/example_seeder.py
```

---

# Integration with Risk Decision Service

The generated:

```text
active-model.json
```

is consumed by:

```text
risk-decision-service
```

Ensure the configured model path matches the generated output location.

---

# Troubleshooting

## Missing Dependencies

Reinstall:

```bash
pip install -r requirements.txt
```

---

## Missing Output Files

Verify:

- pipeline completed successfully
- output/ directory exists

---

## Model Not Loading in Java

Verify:

- model path configuration
- JSON export integrity
- feature parity between Python and Java
