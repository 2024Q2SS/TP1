import json

from matplotlib import pyplot as plt


# Function to plot a circle with handling for wrapping around the edges
def plot_circle(x, y, ax, radius, xlim, ylim, color, fill):
    ax.add_patch(plt.Circle((x, y), radius, fill=fill, color=color))
    if x - radius < 0 and y - radius < 0:
        wrapper_circle = plt.Circle(
            (x + xlim, y + ylim), radius, color=color, fill=fill
        )
        ax.add_patch(wrapper_circle)
    elif x - radius < 0 and y + radius > ylim:
        wrapper_circle = plt.Circle(
            (x + xlim, y - ylim), radius, color=color, fill=fill
        )
        ax.add_patch(wrapper_circle)
    elif x + radius > xlim and y - radius < 0:
        wrapper_circle = plt.Circle(
            (x - xlim, y + ylim), radius, color=color, fill=fill
        )
        ax.add_patch(wrapper_circle)
    elif x + radius > xlim and y + radius > ylim:
        wrapper_circle = plt.Circle(
            (x - xlim, y - ylim), radius, color=color, fill=fill
        )
        ax.add_patch(wrapper_circle)
    elif x - radius < 0:
        wrapper_circle = plt.Circle((x + xlim, y), radius, color=color, fill=fill)
        ax.add_patch(wrapper_circle)
    elif x + radius > xlim:
        wrapper_circle = plt.Circle((x - xlim, y), radius, color=color, fill=fill)
        ax.add_patch(wrapper_circle)
    elif y - radius < 0:
        wrapper_circle = plt.Circle((x, y + ylim), radius, color=color, fill=fill)
        ax.add_patch(wrapper_circle)
    elif y + radius > ylim:
        wrapper_circle = plt.Circle((x, y - ylim), radius, color=color, fill=fill)
        ax.add_patch(wrapper_circle)


# Load the positions from positions.json
with open("./positions.json", "r") as pos_file:
    positions_data = json.load(pos_file)

# Load the configuration from config.json
with open("../config.json", "r") as config_file:
    config = json.load(config_file)

# Extract values from config
L = config["L"]
r_c = config["r_c"]
num_particles = config["N"]

# Calculate cell size
cell_size = L / r_c

# Set up the plot
fig, ax = plt.subplots()
xlim = L
ylim = L

# Access the list of particles in positions_data
particles = positions_data[0]["particles"]

# Plot each particle, using the corresponding radius from config.json
for i, particle in enumerate(particles):
    radius = config["particles"][i]["radius"]  # Get radius from config.json
    plot_circle(
        particle["x"], particle["y"], ax, radius, xlim, ylim, color="blue", fill=True
    )

# Add vertical and horizontal grid lines
for x in range(int(L / cell_size) + 1):
    ax.axvline(x=x * cell_size, color="gray", linestyle="--", alpha=0.5)
for y in range(int(L / cell_size) + 1):
    ax.axhline(y=y * cell_size, color="gray", linestyle="--", alpha=0.5)

ax.set_xlim(0, xlim)
ax.set_ylim(0, ylim)
ax.set_aspect("equal", "box")
plt.savefig("particles_plot.png")
plt.close()
