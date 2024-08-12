package ar.edu.itba.ss;

import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.HashSet;

public class App {

    private static final Map<Integer, Particle> particles = new HashMap<>();

    private static Field field;

    private static final Double defaultRadius = 1.0;

    private static Integer matrixSize;

    private static Double fieldLength;

    private static Integer maxParticles;

    private static Double r_c;

    private static Integer steps = 1;

    public static Coordinates getNewRandCoordinates(final Double minLength,
            final Double maxLength) {
        Double x = minLength + Math.random() * (maxLength - minLength);
        Double y = minLength + Math.random() * (maxLength - minLength);
        return new Coordinates(x, y);
    }

    public static void main(String[] args) {
        String rootDir = System.getProperty("user.dir");
        String configPath = "../config.json";
        String positionsPath = "../positions.json";
        if (args.length > 0) {
            configPath = args[0];
            positionsPath = args[1];
        }
        if (!Paths.get(configPath).isAbsolute()) {
            configPath = Paths.get(rootDir, configPath).toString();
            positionsPath = Paths.get(rootDir, positionsPath).toString();
        }
        Boolean hasRadius = true;

        Gson gson = new Gson();
        try (FileReader positionReader = new FileReader(positionsPath)) {

            try (FileReader configReader = new FileReader(configPath)) {
                JsonObject config = gson.fromJson(configReader, JsonObject.class);
                fieldLength = Objects.isNull(config.get("L")) ? 100.0 : config.get("L").getAsDouble();
                r_c = Objects.isNull(config.get("r_c")) ? 10.0 : config.get("r_c").getAsDouble();
                maxParticles = Objects.isNull(config.get("N")) ? 100 : config.get("N").getAsInt();
                JsonArray particleRadiusArray = config.get("particles") == null ? null
                        : config.get("particles").getAsJsonArray();

                if (particleRadiusArray == null || particleRadiusArray.size() != maxParticles) {
                    hasRadius = false;
                }
                for (int i = 0; i < maxParticles; i++) {

                    Double radius = defaultRadius;
                    if (hasRadius) {
                        radius = particleRadiusArray.get(i).getAsJsonObject().get("radius").getAsDouble();
                    }
                    System.out.println("Particle " + i + " with radius " + radius);
                    Particle particle = new Particle(i, getNewRandCoordinates(0.0, fieldLength), radius);
                    particles.put(i, particle);
                }

            } catch (IOException e) {
                System.err.println("Error reading config file: " + e.getMessage());
            }
        } catch (

        Exception e) {
            // TODO: handle exception
            System.err.println("Error reading position file: " + e.getMessage());
        }
        field = new Field(fieldLength, fieldLength);
        // ESTO TENEMOS QUE REVISARLO
        matrixSize = (int) Math.ceil(fieldLength / r_c);
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                Cell cell = new Cell(i, j);
                field.addCell(cell);
            }
        }

        for (int i = 0; i < steps; i++) {
            sortParticles();
            startCIM();

        }

    }

    public static void sortParticles() {
        for (Particle particle : particles.values()) {
            Integer col = (int) Math.floor(particle.getCoordinates().getX() / (field.getWidth() / matrixSize));
            col = col >= matrixSize ? matrixSize - 1 : col;

            Integer row = (int) Math.floor(particle.getCoordinates().getY() / (field.getHeight() / matrixSize));
            row = row >= matrixSize ? matrixSize - 1 : row;

            Cell cell = field.getCell(row, col);
            cell.addParticle(particle);
            particle.setCell(cell);

        }
        // PRINTS POSITIONS for drawing purposes
        JsonObject mainObject = new JsonObject();
        for (Particle particle : particles.values()) {
            JsonObject particleObject = new JsonObject();
            particleObject.addProperty("id", particle.getId());
            particleObject.addProperty("x", particle.getCoordinates().getX());
            particleObject.addProperty("y", particle.getCoordinates().getY());
            particleObject.addProperty("radius", particle.getRadius());
            mainObject.add(particle.getId().toString(), particleObject);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("position.json")) {
            gson.toJson(mainObject, writer);
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }

    }

    public static List<Particle> getNeighboursFromCell(Cell cell, Particle particle) {
        return cell.getContainedParticles().stream()
                .filter(neighbour -> particle.borderToBorderDistance(neighbour) < r_c).toList();
    }

    public static Set<Particle> getNeighbourParticles(Particle particle, List<Particle> uncheckedParticles) {
        Set<Particle> neighbours = new HashSet<>();
        Cell cell = particle.getCell();

        // analyze top center cell
        Integer topCenterRow = (cell.getRow() + 1) % matrixSize;
        Integer topCenterCol = cell.getCol();

        Cell topCenter = field.getCell(topCenterRow, topCenterCol);
        neighbours.addAll(getNeighboursFromCell(topCenter, particle));
        // analyze top right cell
        Integer topRightRow = (cell.getRow() + 1) % matrixSize;
        Integer topRightCol = (cell.getCol() + 1) % matrixSize;

        Cell topRight = field.getCell(topRightRow, topRightCol);
        neighbours.addAll(getNeighboursFromCell(topRight, particle));
        // analyze middle right cell
        Integer middleRightRow = cell.getRow();
        Integer middleRightCol = (cell.getCol() + 1) % matrixSize;

        Cell middleRight = field.getCell(middleRightRow, middleRightCol);
        neighbours.addAll(getNeighboursFromCell(middleRight, particle));
        // analyze bottom right cell
        Integer bottomRightRow = (cell.getRow() - 1 + matrixSize) % matrixSize;
        Integer bottomRightCol = (cell.getRow() + 1) % matrixSize;

        Cell bottomRight = field.getCell(bottomRightRow, bottomRightCol);
        neighbours.addAll(getNeighboursFromCell(bottomRight, particle));
        // analyze own cell
        neighbours.addAll(uncheckedParticles.stream()
                .filter(neighbour -> particle.borderToBorderDistance(neighbour) < r_c)
                .toList());
        return neighbours;
    }

    public static void startCIM() {
        Map<Integer, Set<Integer>> map = new HashMap<>();

        for (Cell cell : field.getCells()) {
            List<Particle> uncheckedParticles = new ArrayList<>();
            uncheckedParticles.addAll(cell.getContainedParticles());
            for (Particle particle : cell.getContainedParticles()) {
                uncheckedParticles.remove(particle);
                Set<Particle> neighbours = getNeighbourParticles(particle, uncheckedParticles);
                Set<Integer> neighboursIds = map.get(particle.getId());
                if (neighboursIds == null) {
                    neighboursIds = Set
                            .of(neighbours.stream().map(neighbour -> neighbour.getId()).toArray(Integer[]::new));
                } else {
                    neighboursIds.addAll(neighbours.stream().map(neighbour -> neighbour.getId()).toList());
                }
                map.put(particle.getId(), neighboursIds);
            }
        }

        // OUTPUT JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("neighbours.json")) {
            gson.toJson(map, writer);
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }
}
