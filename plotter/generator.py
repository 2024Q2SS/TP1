import json
import uuid

import numpy as np


def distance(p1, p2):
    return np.sqrt((p1[0] - p2[0]) ** 2 + (p1[1] - p2[1]) ** 2)


def generate_random_coords(n, space_size, radii):
    coords = []
    attempts = 0

    while len(coords) < n and attempts < 1000000:
        x = np.round(
            np.random.uniform(radii[len(coords)], space_size[0] - radii[len(coords)]), 1
        )
        y = np.round(
            np.random.uniform(radii[len(coords)], space_size[1] - radii[len(coords)]), 1
        )
        new_coord = (x, y)

        # Check if it overlaps with any existing particles
        overlap = False
        for i, coord in enumerate(coords):
            if distance(coord, new_coord) < radii[i] + radii[len(coords)]:
                overlap = True
                break

        if not overlap:
            coords.append(new_coord)
        attempts += 1
    if len(coords) < n:
        print("Could not place all particles without overlap.")
    return coords


def save_coords_to_json(coords, filename="../positions.json"):
    particles_list = [{"x": coord[0], "y": coord[1]} for coord in coords]
    data = {"particles": particles_list}

    with open(filename, "w") as f:
        json.dump([data], f, indent=4)


def gen_random_radius(n, min_radius, max_radius):
    return np.random.uniform(min_radius, max_radius, n)


def save_radius_to_config_json(filename="../config.json", radii=[]):
    with open(filename, "r") as f:
        config = json.load(f)
        config["particles"] = [{"radius": radius} for radius in radii]
        with open(filename, "w") as f:
            json.dump(config, f, indent=4)


# Example Usage:
n_particles = 10
space_size = (100, 100)
min_radius = 0.25
max_radius = 0.25

seed = uuid.uuid4().int & (1 << 32) - 1

with open("../config.json", "r") as f:
    config = json.load(f)
    n_particles = config["N"]
    space_size = (config["L"], config["L"])
    if "seed" in config and config["seed"] is not None:
        seed = config["seed"]
    else:
        config["seed"] = seed
        with open("../config.json", "w") as f:
            json.dump(config, f, indent=4)
            print(f"Seed: {seed} saved in config file")


radii = gen_random_radius(
    n_particles, min_radius, max_radius
)  # All particles have the same radius of 5 units
save_radius_to_config_json(radii=radii)
coordinates = generate_random_coords(n_particles, space_size, radii)
save_coords_to_json(coordinates)
