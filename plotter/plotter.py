# Plot circles and add text
import numpy as np
import pandas as pd
from matplotlib import pyplot as plt


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


df = pd.read_json("neighbours.json")
radius = 1
xlim = 100
ylim = 100
already_plotted = []
fig, ax = plt.subplots()
main_id = np.random.choice(df.columns)

plot_circle(df[main_id].x, df[main_id].y, ax, radius, xlim, ylim, "red", True)
already_plotted.append(main_id)


for neighbour in df[main_id].neighbours:
    neighbour_id = neighbour["id"]
    plot_circle(
        df[neighbour_id].x,
        df[neighbour_id].y,
        ax,
        radius,
        xlim,
        ylim,
        "blue",
        False,
    )
    ax.plot(
        [df[main_id].x, df[neighbour_id].x], [df[main_id].y, df[neighbour_id].y], "k-"
    )
    already_plotted.append(neighbour_id)

for i in range(0, 99):
    is_neighbour = False
    if i in already_plotted:
        continue
    neighbour_list = df[i].neighbours
    for neighbour in neighbour_list:
        neighbour_id = neighbour["id"]
        if neighbour_id == main_id:
            is_neighbour = True
            plot_circle(
                df[i].x,
                df[i].y,
                ax,
                radius,
                xlim,
                ylim,
                "blue",
                False,
            )
            ax.plot([df[i].x, df[main_id].x], [df[i].y, df[main_id].y], "k-")
            break
    if is_neighbour:
        continue
    plot_circle(df[i].x, df[i].y, ax, radius, xlim, ylim, "black", False)
    already_plotted.append(i)

ax.set_xlim(0, xlim)
ax.set_ylim(0, ylim)
ax.set_aspect("equal", "box")
plt.savefig("neighbours.png")
plt.close()
