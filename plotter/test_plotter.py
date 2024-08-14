import argparse
import re

import matplotlib.pyplot as plt
import pandas as pd

parser = argparse.ArgumentParser(description="Process some CIM results and plot them.")
parser.add_argument(
    "with_brute_force", type=str, help="run with brute force method too"
)


args = parser.parse_args()

use_brute_force = args.with_brute_force == "True"

# Load the CSV file
df = (
    pd.read_csv("results_with_brute_force.csv")
    if use_brute_force
    else pd.read_csv("results.csv")
)

# Assuming 'config' is a string like 'N-M', we need to separate it into N and M
df[["N", "M"]] = df["config"].str.extract(r"N(\d+)M(\d+)").astype(int)

# Group the data by 'N' and plot the evolution of 'M'
unique_N = df["N"].unique()

for n in unique_N:
    subset = df[df["N"] == n]
    plt.figure(figsize=(10, 6))
    if use_brute_force:
        # Plotting brute force times with error bars
        plt.errorbar(
            subset["M"],
            subset["mean_brute_force_time"],
            yerr=subset["std_brute_force_time"],
            label="Brute Force Times",
            marker="o",
            capsize=5,
            color="red",
        )

    # Plotting CIM wrapped times with error bars
    plt.errorbar(
        subset["M"],
        subset["mean_cim_wrapped_time"],
        yerr=subset["std_cim_wrapped_time"],
        label="CIM Wrapped Times",
        marker="o",
        capsize=5,
        color="blue",
    )

    plt.xlabel("M")
    plt.ylabel("Time")
    plt.title(f"Time Evolution for N = {n}")
    plt.legend()
    plt.grid(True)
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig(f"time_evolution_N{n}.png")
