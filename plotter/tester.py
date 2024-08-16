import argparse
import csv
import json
import os
import subprocess

import numpy as np

parser = argparse.ArgumentParser(description="Process some CIM runs.")
parser.add_argument(
    "with_brute_force", type=str, help="run with brute force method too"
)


args = parser.parse_args()

use_brute_force = args.with_brute_force == "True"
# Define the path for the CSV file
csv_file = ""

mvn_command = []

# Check if the CSV file exists to decide whether to write the header
write_header = not os.path.exists(csv_file)

# Generator command
generator = ["python", "generator.py"]
path_to_config = "../config.json"
# Loop setUp
N = [50, 100, 150, 200]
M = [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
# N = [100, 200]
# M = [2, 13]
# Write the header if needed
# Write the data row
data = []
brute_force_times = []
cim_wrapped_times = []

if use_brute_force:
    csv_file = "results_with_brute_force.csv"
    # mvn_command = [
    #     "mvn",
    #     "exec:java",
    #     "-Dexec.mainClass=ar.edu.itba.ss.App",
    #     "-Dexec.args=--brute-force --cim --wrap-borders",
    # ]
    mvn_command = [
        "java",
        "-jar",
        "target/CIM-1.0-SNAPSHOT.jar",
        "--brute-force",
        "--cim",
        " --wrap-borders",
    ]
    data.append(
        [
            "config",
            "mean_brute_force_time",
            "mean_cim_wrapped_time",
            "std_brute_force_time",
            "std_cim_wrapped_time",
        ]
    )
else:
    csv_file = "results.csv"
    mvn_command = [
        "java",
        "-jar",
        "target/CIM-1.0-SNAPSHOT.jar",
        "--wrap-borders",
    ]
    data.append(
        [
            "config",
            "mean_cim_wrapped_time",
            "std_cim_wrapped_time",
        ]
    )


for n in N:
    with open(path_to_config, "r") as config_file:
        config = json.load(config_file)
        config["N"] = n
        with open(path_to_config, "w") as file:
            json.dump(config, file, indent=4)
    subprocess.run(generator)
    for m in M:
        print(f"Running test with N = {n}, M = {m}")
        with open(path_to_config, "r") as config_file:
            config = json.load(config_file)
            config["M"] = m
            with open(path_to_config, "w") as file:
                json.dump(config, file, indent=4)
        config = f"N{n}M{m}"
        os.chdir("../CIM")
        for i in range(0, 10):
            # Run the Maven command
            result = subprocess.run(mvn_command, capture_output=True, text=True)

            # Split the output into lines
            output_lines = result.stdout.splitlines()

            # Extract lines 14 and 16 (indexing starts at 0)
            if use_brute_force:
                brute_force_time = output_lines[6] if len(output_lines) > 8 else None
                brute_force_times.append(brute_force_time)
            cim_wrapped_time = (
                output_lines[8 if use_brute_force else 6]
                if len(output_lines) > 6
                else None
            )
            cim_wrapped_times.append(cim_wrapped_time)

        cim_wrapped_times = [float(x) for x in cim_wrapped_times]
        if use_brute_force:
            brute_force_times = [float(x) for x in brute_force_times]
            data.append(
                [
                    config,
                    np.mean(brute_force_times),
                    np.mean(cim_wrapped_times),
                    np.std(brute_force_times) / np.sqrt(len(brute_force_times)),
                    np.std(cim_wrapped_times) / np.sqrt(len(cim_wrapped_times)),
                ]
            )
            brute_force_times = []
        else:
            data.append(
                [
                    config,
                    np.mean(cim_wrapped_times),
                    np.std(cim_wrapped_times) / np.sqrt(len(cim_wrapped_times)),
                ]
            )
        cim_wrapped_times = []
    os.chdir("../plotter")

file = open(csv_file, "w", newline="")
csv.writer(file).writerows(data)

# Print confirmation
print(f"Results saved to {csv_file}")
