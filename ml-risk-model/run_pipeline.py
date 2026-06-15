# ==========================================
# ML PIPELINE RUNNER
# ==========================================

"""
PURPOSE:
--------
Runs entire ML pipeline in correct order.

PIPELINE:
---------
1. Generate raw transactions
2. Generate behavioral features
3. Generate fraud labels
4. Validate dataset
5. Train model
6. Export final model

USAGE:
------
python run_pipeline.py
"""

import subprocess
import sys
from pathlib import Path

# ==========================================
# PROJECT ROOT
# ==========================================

PROJECT_ROOT = Path(__file__).parent

SCRIPTS_DIR = PROJECT_ROOT / "scripts"

PYTHON_EXECUTABLE = str(
    PROJECT_ROOT / "venv/bin/python"
)

# ==========================================
# PIPELINE ORDER
# ==========================================

PIPELINE_STEPS = [

    "user_transaction_simulator.py",

    "behavior_features.py",

    "label_generator.py",

    "dataset_validator.py",

    "train_model.py"
]

# ==========================================
# RUN SINGLE SCRIPT
# ==========================================

def run_script(script_name):

    script_path = SCRIPTS_DIR / script_name

    print("\n" + "=" * 60)
    print(f"RUNNING: {script_name}")
    print("=" * 60)

    result = subprocess.run(
        [PYTHON_EXECUTABLE, str(script_path)],
        cwd=PROJECT_ROOT
    )

    if result.returncode != 0:

        raise RuntimeError(
            f"Pipeline failed while executing: {script_name}"
        )

    print(f"COMPLETED: {script_name}")

# ==========================================
# RUN ENTIRE PIPELINE
# ==========================================

def run_pipeline():

    print("\nSTARTING ML TRAINING PIPELINE")

    for step in PIPELINE_STEPS:

        run_script(step)

    print("\n" + "=" * 60)
    print("ML PIPELINE COMPLETED SUCCESSFULLY")
    print("=" * 60)

# ==========================================
# ENTRY POINT
# ==========================================

if __name__ == "__main__":

    run_pipeline()