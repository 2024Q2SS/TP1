package ar.edu.itba.ss;

/**
 * Hello world!
 *
 */

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.FileWriter;
import java.io.IOException;

public class App {

    private static final Map<Integer, Particle> particles = new HashMap<>();

    private static Field field;

    private static Integer matrixSize;

    private static Integer maxParticles;

    private static Double r_c;

    public static Coordinates getNewRandCoordinates(final Double minWidth, final Double maxWidth,
            final Double maxHeight) {
        Double x = minWidth + Math.random() * (maxWidth - minWidth);
        Double y = minWidth + Math.random() * (maxHeight - minWidth);
        return new Coordinates(x, y);
    }

    public static void main(String[] args) {
        Double fieldWidth = 100.0;
        Double fieldHeight = 100.0;
        r_c = 10.0;
        maxParticles = 100;
        field = new Field(fieldWidth, fieldHeight);
        matrixSize = (int) Math.ceil(fieldWidth / r_c);
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                Cell cell = new Cell(i, j);
                field.addCell(cell);
            }
        }

        for (int i = 0; i < maxParticles; i++) {
            Particle particle = new Particle(i, getNewRandCoordinates(0.0, fieldWidth, fieldHeight));
            particles.put(i, particle);
        }
        // Create Gson instance
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convert HashMap to JSON
        String json = gson.toJson(particles);

        // Write JSON to file
        //
        // try (FileWriter writer = new FileWriter("data.json")) {
        // writer.write(json);
        // System.out.println("Successfully wrote JSON to file.");
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        sortParticles();
        startCIM();

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
    }

    public static List<Particle> getNeighbourParticles(Particle particle) {
        List<Particle> neighbours = new ArrayList<>();
        Cell cell = particle.getCell();

        // analyze top center cell
        Integer topCenterRow = (cell.getRow() + 1) % matrixSize;
        Integer topCenterCol = cell.getCol();

        Cell topCenter = field.getCell(topCenterRow, topCenterCol);
        neighbours.addAll(topCenter.getContainedParticles().stream()
                .filter(neighbour -> particle.getCoordinates().euclideanDistance(neighbour.getCoordinates()) < r_c)
                .toList());
        // analyze top right cell
        Integer topRightRow = (cell.getRow() + 1) % matrixSize;
        Integer topRightCol = (cell.getCol() + 1) % matrixSize;

        Cell topRight = field.getCell(topRightRow, topRightCol);
        neighbours.addAll(topRight.getContainedParticles().stream()
                .filter(neighbour -> particle.getCoordinates().euclideanDistance(neighbour.getCoordinates()) < r_c)
                .toList());
        // analyze middle right cell
        Integer middleRightRow = cell.getRow();
        Integer middleRightCol = (cell.getCol() + 1) % matrixSize;

        Cell middleRight = field.getCell(middleRightRow, middleRightCol);
        neighbours.addAll(middleRight.getContainedParticles().stream()
                .filter(neighbour -> particle.getCoordinates().euclideanDistance(neighbour.getCoordinates()) < r_c)
                .toList());
        // analyze bottom right cell
        Integer bottomRightRow = (cell.getRow() - 1 + matrixSize) % matrixSize;
        Integer bottomRightCol = (cell.getRow() + 1) % matrixSize;

        Cell bottomRight = field.getCell(bottomRightRow, bottomRightCol);
        neighbours.addAll(bottomRight.getContainedParticles().stream()
                .filter(neighbour -> particle.getCoordinates().euclideanDistance(neighbour.getCoordinates()) < r_c)
                .toList());

        return neighbours;
    }

    public static void startCIM() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject mainObject = new JsonObject();
        for (Cell cell : field.getCells()) {
            List<Particle> alreadyChecked = new ArrayList<>();
            for (Particle particle : cell.getContainedParticles()) {
                alreadyChecked.add(particle);
                List<Particle> neighbours = getNeighbourParticles(particle);
                neighbours.addAll(cell.getContainedParticles()
                        .stream().filter(p -> !alreadyChecked.contains(p)).toList());
                JsonObject particleObject = new JsonObject();
                JsonArray neighboursArray = new JsonArray();
                for (Particle neighbour : neighbours) {
                    JsonObject neighbourObject = new JsonObject();
                    neighbourObject.addProperty("id", neighbour.getId());
                    neighbourObject.addProperty("x", neighbour.getCoordinates().getX());
                    neighbourObject.addProperty("y", neighbour.getCoordinates().getY());
                    neighboursArray.add(neighbourObject);

                }
                particleObject.addProperty("id", particle.getId());
                particleObject.addProperty("x", particle.getCoordinates().getX());
                particleObject.addProperty("y", particle.getCoordinates().getY());
                particleObject.add("neighbours", neighboursArray);
                mainObject.add(particle.getId().toString(), particleObject);
            }
        }
        String json = gson.toJson(mainObject);
        try (FileWriter writer = new FileWriter("neighbours.json")) {
            writer.write(json);
            System.out.println("Successfully wrote JSON to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
