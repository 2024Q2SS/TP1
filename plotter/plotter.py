import json
import matplotlib.pyplot as plt
import numpy as np

def plot_circle(x, y, ax, radius, xlim, ylim, color, fill):
    # Function to add a circle and its wrapped counterparts
    def add_circle(x_center, y_center):
        ax.add_patch(plt.Circle((x_center, y_center), radius, fill=fill, color=color))

    # Main circle
    add_circle(x, y)

    # Check and wrap around x-axis
    if x - radius < 0:
        add_circle(x + xlim, y)  # Left to Right wrap
    if x + radius > xlim:
        add_circle(x - xlim, y)  # Right to Left wrap

    # Check and wrap around y-axis
    if y - radius < 0:
        add_circle(x, y + ylim)  # Bottom to Top wrap
    if y + radius > ylim:
        add_circle(x, y - ylim)  # Top to Bottom wrap

    # Check and wrap around both axes (corners)
    if x - radius < 0 and y - radius < 0:
        add_circle(x + xlim, y + ylim)  # Bottom-left to Top-right wrap
    if x + radius > xlim and y - radius < 0:
        add_circle(x - xlim, y + ylim)  # Bottom-right to Top-left wrap
    if x - radius < 0 and y + radius > ylim:
        add_circle(x + xlim, y - ylim)  # Top-left to Bottom-right wrap
    if x + radius > xlim and y + radius > ylim:
        add_circle(x - xlim, y - ylim)  # Top-right to Bottom-left wrap


# Load the positions from positions.json
with open("../positions.json", "r") as pos_file:
    positions_data = json.load(pos_file)

# Load the configuration from config.json
with open("../config.json", "r") as config_file:
    config = json.load(config_file)

with open("../CIM/CIM_neighbours.json", "r") as neighbours:
    neighbours_json = json.load(neighbours)

# Extract values from config
L = config["L"]
r_c = config["r_c"]
num_particles = config["N"]
main = config["Main"]

# Get the neighbours list of the particle with id `main`
neighbours = neighbours_json[str(main)]
# Calculate cell size
cell_size = L / (r_c + 0.5)

# Set up the plot
fig, ax = plt.subplots()
xlim = L
ylim = L

# Access the list of particles in positions_data
particles = positions_data[0]["particles"]
main_radius = config["particles"][main]["radius"]


# Function to update the plot
def update_plot(main):
    ax.clear()
    neighbours = neighbours_json[str(main)]

    # Plot each particle
    for i, particle in enumerate(particles):
        radius = config["particles"][i]["radius"]
        aux_color = "black"
        if i == main:
            aux_color = "red"
            plot_circle(
                    particle["x"],
                    particle["y"],
                    ax,
                    r_c + main_radius,
                    xlim,
                    ylim,
                    color="grey",
                    fill=False)
        if i in neighbours:
            aux_color = "blue"
        plot_circle(
            particle["x"], particle["y"], ax, radius, xlim, ylim, aux_color, fill=True
        )
        #ax.text(particle["x"]+0.1,particle["y"]+0.1,particle["id"],fontsize=10,color="black")

    # Add vertical and horizontal grid lines
    cell_limits = [round(L / cell_size, 1) * i for i in range(int(cell_size) + 1)]
    for x in cell_limits:
        ax.axvline(x=x, color="gray", linestyle="--", alpha=0.5)
    for y in cell_limits:
        ax.axhline(y=y, color="gray", linestyle="--", alpha=0.5)

    ax.set_xlim(0, xlim)
    ax.set_ylim(0, ylim)
    ax.set_aspect("equal", "box")
    plt.draw()

# Function to handle mouse click events
def on_click(event):
    if event.inaxes is not None:
        # Find the particle closest to the click position
        click_x, click_y = event.xdata, event.ydata
        distances = [(i, np.hypot(p["x"] - click_x, p["y"] - click_y)) for i, p in enumerate(particles)]
        closest_particle = min(distances, key=lambda t: t[1])[0]

        # Update plot with the new main particle
        update_plot(closest_particle)

# Initial plot
update_plot(main)

# Connect the click event to the on_click function
fig.canvas.mpl_connect("button_press_event", on_click)

plt.show()
