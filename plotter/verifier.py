import json


def load_json_file(file_path):
    with open(file_path, "r") as file:
        return json.load(file)


def compare_json_files(file1, file2):
    # Load JSON data
    data1 = load_json_file(file1)
    data2 = load_json_file(file2)

    # Check if both JSON files have the same keys
    if data1.keys() != data2.keys():
        print("JSON files have different keys.")
        return False

    # Compare the arrays for each key
    for key in data1:
        array1 = sorted(data1[key])
        array2 = sorted(data2[key])

        if array1 != array2:
            print(f"Arrays for key '{key}' differ.")
            return False

    print("JSON files are identical.")
    return True


# Paths to the JSON files
file1 = "../CIM/CIM_neighbours.json"
file2 = "../CIM/brute_force_neighbours.json"

# Compare the JSON files
compare_json_files(file1, file2)
