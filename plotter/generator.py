import json
import random
import math

# Función para verificar si dos partículas se superponen
def is_overlapping(p1, p2):
    distance = math.sqrt((p1["x"] - p2["x"]) ** 2 + (p1["y"] - p2["y"]) ** 2)
    return distance < (p1["radius"] + p2["radius"])

# Leer la configuración desde el archivo config.json
with open('../config.json', 'r') as config_file:
    config = json.load(config_file)

# Configurar la semilla
if "seed" in config:
    random.seed(config["seed"])
else:
    seed = random.randint(0, 1000000)
    random.seed(seed)
    config["seed"] = seed
    print(f"Semilla aleatoria generada: {seed}")

# Definir los límites para las coordenadas
L = config.get("L", 20)

# Leer el array de partículas y sus radios desde el archivo de configuración
particles_config = config["particles"]

# Generar partículas con coordenadas aleatorias sin superposición
particles = []
for particle_config in particles_config:
    particle = {}
    while True:
        particle = {
            "x": round(random.uniform(0, L), 1),
            "y": round(random.uniform(0, L), 1),
            "radius": particle_config["radius"]
        }
        
        # Verificar si la partícula se superpone con alguna existente
        if not any(is_overlapping(particle, p) for p in particles):
            break  # Si no hay superposición, sal del loop y añade la partícula

    particles.append(particle)

# Crear el JSON de salida con el formato requerido
output = [{"particles": particles}]

# Guardar el JSON de salida en un archivo
with open('positions.json', 'w') as output_file:
    json.dump(output, output_file, indent=4)

print("Archivo JSON generado con éxito.")
