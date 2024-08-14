import json
import math
import random


# Función para verificar si dos partículas se superponen
def is_overlapping(p1, p2):
    distance = math.sqrt((p1["x"] - p2["x"]) ** 2 + (p1["y"] - p2["y"]) ** 2)
    return distance < (p1["radius"] + p2["radius"])


# Leer la configuración desde el archivo config.json
with open("../config.json", "r") as config_file:
    config = json.load(config_file)

# Configurar la semilla
if "seed" in config:
    random.seed(config["seed"])
else:
    seed = random.randint(0, 1000000)
    random.seed(seed)
    config["seed"] = seed
    print(f"Semilla aleatoria generada: {seed}")

# Definir el tamaño del tablero L
L = config.get("L", 20)

# Definir el radio máximo permitido (máximo de 1)
max_radius = 0.25
min_radius = 0.25
# Obtener la cantidad de partículas N
N = config.get("N", len(config.get("particles", [])))

# Asegurar que la cantidad de partículas en config.json sea N
if "particles" not in config or len(config["particles"]) != N:
    config["particles"] = [{"radius": 0} for _ in range(N)]

# Modificar o añadir los radios en el archivo config.json
for particle_config in config["particles"]:
    # Generar un radio aleatorio no mayor que 1
    radius = round(random.uniform(min_radius, max_radius), 2)
    particle_config["radius"] = radius

# Guardar el archivo config.json modificado
with open("../config.json", "w") as config_file:
    json.dump(config, config_file, indent=4)

print("Archivo config.json modificado con éxito.")

# Generar partículas con radios y coordenadas aleatorias sin superposición
particles = []
for particle_config in config["particles"]:
    while True:
        particle = {
            "x": round(
                random.uniform(
                    particle_config["radius"], L - particle_config["radius"]
                ),
                1,
            ),
            "y": round(
                random.uniform(
                    particle_config["radius"], L - particle_config["radius"]
                ),
                1,
            ),
            "radius": particle_config[
                "radius"
            ],  # Use the updated radius from config.json for placement
        }

        # Verificar si la partícula se superpone con alguna existente
        if not any(is_overlapping(particle, p) for p in particles):
            break  # Si no hay superposición, sal del loop y añade la partícula

    particles.append(particle)

# Crear el JSON de salida con las posiciones en el formato especificado
output = [{"particles": [{"x": p["x"], "y": p["y"]} for p in particles]}]

# Guardar el JSON de salida en un archivo
with open("positions.json", "w") as output_file:
    json.dump(output, output_file, indent=4)

print("Archivo positions.json generado con éxito.")
