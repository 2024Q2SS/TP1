package ar.edu.itba.ss;

import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.HashSet;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

public class App {

    // Hola soy un comentario
    private static final Map<Integer, Particle> particles = new HashMap<>();

    private static Field field;

    private static final Double defaultRadius = 1.0;

    private static Integer M = null;

    private static Double fieldLength = 100.0;

    private static Integer maxParticles = 100;

    private static Double r_c = 10.0;

    private static String rootDir = System.getProperty("user.dir");

    private static String configPath = "../config.json";

    private static String positionsPath = "../positions.json";

    private static Gson gson = new Gson();

    private static Boolean hasRadius = false;

    private static Double maxRadius = 1.0;
    private static Double minRadius = 1.0;

    private static Boolean useBruteForce = false;
    private static Boolean useCIM = true;

    private static Boolean wrapBorders = false;

    public static Coordinates getNewRandCoordinates(final Double minLength,
            final Double maxLength) {
        Double x = minLength + Math.random() * (maxLength - minLength);
        Double y = minLength + Math.random() * (maxLength - minLength);
        return new Coordinates(x, y);
    }

    public static void setUp() {
        try (FileReader configReader = new FileReader(configPath)) {
            JsonObject config = gson.fromJson(configReader, JsonObject.class);
            fieldLength = Objects.isNull(config.get("L")) ? 100.0 : config.get("L").getAsDouble();
            r_c = Objects.isNull(config.get("r_c")) ? 10.0 : config.get("r_c").getAsDouble();
            maxParticles = Objects.isNull(config.get("N")) ? 100 : config.get("N").getAsInt();
            hasRadius = !Objects.isNull(config.get("particles"));
            M = Objects.isNull(config.get("M")) ? null : config.get("M").getAsInt();

        } catch (IOException e) {
            System.err.println("Config file not found, using defaults");
        }
    }

    public static void createParticles() {
        if (hasRadius) {
            try (FileReader radiusReader = new FileReader(configPath)) {
                JsonObject radius = gson.fromJson(radiusReader, JsonObject.class);
                JsonArray particlesArray = radius.get("particles").getAsJsonArray();
                for (int i = 0; i < maxParticles; i++) {
                    Double radiusValue = particlesArray.get(i).getAsJsonObject().get("radius").getAsDouble();
                    if (i == 0) {
                        maxRadius = radiusValue;
                        minRadius = radiusValue;

                    } else {
                        if (radiusValue > maxRadius) {
                            maxRadius = radiusValue;
                        }
                        if (radiusValue < minRadius) {
                            minRadius = radiusValue;
                        }

                    }
                    Particle particle = new Particle(i, radiusValue);
                    particles.put(i, particle);
                }
            } catch (IOException e) {
                System.err.println("Error reading radius file: " + e.getMessage());
            }
        } else {
            for (int i = 0; i < maxParticles; i++) {
                Particle particle = new Particle(i, defaultRadius);
                particles.put(i, particle);
            }
        }
    }

    public static void main(String[] args) {

        Options options = new Options();

        // Define the options
        options.addOption(Option.builder()
                .longOpt("config-path")
                .option("c")
                .desc("Path to the configuration file")
                .hasArg()
                .argName("FILE")
                .required(false)
                .build());

        options.addOption(Option.builder()
                .option("p")
                .longOpt("positions-path")
                .desc("Path to the positions file")
                .hasArg()
                .argName("FILE")
                .required(false)
                .build());

        options.addOption(Option.builder()
                .longOpt("brute-force")
                .desc("Enable brute-force mode")
                .hasArg(false)
                .required(false)
                .build());

        options.addOption(Option.builder()
                .longOpt("cim")
                .desc("Enable CIM mode")
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder()
                .longOpt("wrap-borders")
                .desc("Make field borders wrapped")
                .hasArg(false)
                .required(false)
                .build());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            // Parse the command line arguments
            CommandLine cmd = parser.parse(options, args);

            // Extract the option values
            if (cmd.hasOption("config-path"))
                configPath = cmd.getOptionValue("config-path");
            if (cmd.hasOption("positions-path"))
                positionsPath = cmd.getOptionValue("positions-path");
            useBruteForce = cmd.hasOption("brute-force");
            useCIM = cmd.hasOption("cim");
            if (useBruteForce && !useCIM) {
                useCIM = false;
            }
            wrapBorders = cmd.hasOption("wrap-borders");
        } catch (ParseException e) {
            System.err.println("Error parsing command line options: " + e.getMessage());
            formatter.printHelp("MainApp", options);
        }

        if (!Paths.get(configPath).isAbsolute()) {
            configPath = Paths.get(rootDir, configPath).toString();
        }
        if (!Paths.get(positionsPath).isAbsolute()) {
            positionsPath = Paths.get(rootDir, positionsPath).toString();
        }

        setUp();
        System.out.println("set up done");
        createParticles();
        System.out.println("particles created");
        field = new Field(fieldLength, fieldLength);
        if (Objects.isNull(M))
            M = (int) Math.floor(fieldLength / (r_c + (maxRadius + minRadius)));
        System.out.println("Using matrix size: " + M);
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                field.addCell(new Cell(i, j));
            }
        }
        System.out.println("field created");
        start();
    }

    public static void start() {
        System.out.println("Starting...");
        try (FileReader positionsFile = new FileReader(positionsPath)) {
            JsonArray positions = gson.fromJson(positionsFile, JsonArray.class)
                    .get(0).getAsJsonObject()
                    .get("particles").getAsJsonArray();
            for (int i = 0; i < maxParticles; i++) {
                JsonObject particle = positions.get(i).getAsJsonObject();
                Double x = particle.get("x").getAsDouble();
                Double y = particle.get("y").getAsDouble();
                Coordinates coordinates = new Coordinates(x, y);
                particles.get(i).setCoordinate(coordinates);
            }

        } catch (IOException e) {
            System.err.println("No positions file found or its badly formatted, generating random positions");
            for (int i = 0; i < maxParticles; i++) {
                Coordinates coordinates = getNewRandCoordinates(0.0, fieldLength);
                particles.get(i).setCoordinate(coordinates);
            }

        }
        if (useBruteForce) {
            System.out.println("Using Brute Force");
            Long start = System.nanoTime();
            startBruteForce();
            Long end = System.nanoTime();
            System.out.println("Time elapsed: " + (end - start) / 1000000 + "ms");
        }
        if (useCIM) {
            if (wrapBorders) {

                System.out.println("Using Wrapped CIM");
                Long start = System.nanoTime();
                sortParticlesWrapped();
                startCIMWrapped();
                Long end = System.nanoTime();
                System.out.println("Time elapsed: " + (end - start) / 1000000 + "ms");

            } else {
                System.out.println("Using CIM");
                Long start = System.nanoTime();
                sortParticles();
                startCIM();
                Long end = System.nanoTime();
                System.out.println("Time elapsed: " + (end - start) / 1000000 + "ms");

            }

        }
    }

    public static void sortParticles() {
        Double cellLength = fieldLength / M;
        for (Particle particle : particles.values()) {
            Double xLeftLimit = particle.getCoordinates().getX() - particle.getRadius();
            Double xRightLimit = particle.getCoordinates().getX() + particle.getRadius();
            Double yLeftLimit = particle.getCoordinates().getY() - particle.getRadius();
            Double yRightLimit = particle.getCoordinates().getY() + particle.getRadius();

            if (xLeftLimit < 0.0 || xRightLimit > fieldLength || yLeftLimit < 0.0 || yRightLimit > fieldLength)
                throw new IllegalArgumentException("Particle " + particle.getId() + " is out of bounds");

            Integer row = (int) Math.floor(particle.getCoordinates().getX() / cellLength);
            row = row >= M ? M - 1 : row;

            Integer col = (int) Math.floor(particle.getCoordinates().getY() / cellLength);
            col = col >= M ? M - 1 : col;
            Cell cell = field.getCell(row, col);
            cell.addParticle(particle);
            particle.setCell(cell);
        }
    }

    public static void startCIM() {
        Map<Integer, Set<Integer>> map = new HashMap<>();
        Set<Particle> uncheckedParticles = new HashSet<>();
        for (Cell cell : field.getCells()) {
            uncheckedParticles.addAll(cell.getContainedParticles());
            for (Particle particle : cell.getContainedParticles()) {
                uncheckedParticles.remove(particle);
                // OWN CELL
                uncheckedParticles.stream().forEach((other) -> {
                    if (particle.borderToBorderDistance(other) <= r_c) {
                        addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                    }
                });
                Integer bottomRow = cell.getRow() - 1;
                Integer topRow = cell.getRow() + 1;
                Integer middleRow = cell.getRow();
                Integer rightCol = cell.getCol() + 1;
                Integer middleCol = cell.getCol();
                if (rightCol < M) {
                    try {

                        Cell bottomRight = field.getCell(bottomRow, rightCol);
                        bottomRight.getContainedParticles().stream().forEach((other) -> {
                            if (particle.borderToBorderDistance(other) <= r_c) {
                                addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                            }
                        });
                    } catch (Exception e) {
                    }
                    try {

                        Cell middleRight = field.getCell(middleRow, rightCol);
                        middleRight.getContainedParticles().stream().forEach((other) -> {
                            if (particle.borderToBorderDistance(other) <= r_c) {
                                addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                            }
                        });
                    } catch (Exception e) {
                    }
                    try {
                        Cell topRight = field.getCell(topRow, rightCol);
                        topRight.getContainedParticles().stream().forEach((other) -> {
                            if (particle.borderToBorderDistance(other) <= r_c) {
                                addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                            }
                        });

                    } catch (Exception e) {
                    }
                }

                try {
                    Cell topCenter = field.getCell(topRow, middleCol);
                    topCenter.getContainedParticles().stream().forEach((other) -> {
                        if (particle.borderToBorderDistance(other) <= r_c) {
                            addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                        }
                    });

                } catch (Exception e) {
                }

                if (map.get(particle.getId()) == null) {
                    map.put(particle.getId(), new HashSet<>());
                }
            }
            uncheckedParticles.clear();
        }

        // OUTPUT JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("CIM_neighbours.json")) {
            gson.toJson(map, writer);
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }

    }

    public static void startBruteForce() {
        Map<Integer, Set<Integer>> map = new HashMap<>();
        for (Particle particle : particles.values()) {
            for (Particle other : particles.values()) {
                if (particle.getId() == other.getId())
                    continue;
                if (particle.getId() != other.getId() && particle.borderToBorderDistance(other) <= r_c) {
                    addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                }
            }
            if (map.get(particle.getId()) == null) {
                map.put(particle.getId(), new HashSet<>());
            }

        }
        // OUTPUT JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("brute_force_neighbours.json")) {
            gson.toJson(map, writer);
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }

    public static void sortParticlesWrapped() {
        Double cellLength = fieldLength / M;
        for (Particle particle : particles.values()) {

            Integer row = (int) Math.floor(particle.getCoordinates().getX() / cellLength);
            row = row >= M ? 0 : row;
            Integer col = (int) Math.floor(particle.getCoordinates().getY() / cellLength);
            col = col >= M ? 0 : col;
            Cell cell = field.getCell(row, col);
            cell.addParticle(particle);
            particle.setCell(cell);
        }
    }

    public static void addNeighbourRelationsToMap(Map<Integer, Set<Integer>> map, Integer particleId, Integer otherId) {
        Set<Integer> neighboursIds = map.get(particleId);
        if (neighboursIds == null) {
            neighboursIds = new HashSet<>();
        }
        Set<Integer> othersNeighbours = map.get(otherId);
        if (othersNeighbours == null) {
            othersNeighbours = new HashSet<>();
        }
        neighboursIds.add(otherId);
        othersNeighbours.add(particleId);

        map.put(otherId, othersNeighbours);
        map.put(particleId, neighboursIds);
    }

    public static void startCIMWrapped() {
        Map<Integer, Set<Integer>> map = new HashMap<>();
        Set<Particle> uncheckedParticles = new HashSet<>();
        for (Cell cell : field.getCells()) {
            uncheckedParticles.addAll(cell.getContainedParticles());
            for (Particle particle : cell.getContainedParticles()) {
                uncheckedParticles.remove(particle);
                // OWN CELL
                uncheckedParticles.stream().forEach((other) -> {
                    if (particle.borderToBorderDistance(other) <= r_c) {
                        addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                    }
                });
                Integer bottomRow = cell.getRow() == 0 ? M - 1 : cell.getRow() - 1;
                Integer topRow = (cell.getRow() + 1) % M;
                Integer middleRow = cell.getRow();
                Integer rightCol = (cell.getCol() + 1) % M;
                Integer middleCol = cell.getCol();

                Cell bottomRight = field.getCell(bottomRow, rightCol);
                Cell middleRight = field.getCell(middleRow, rightCol);
                Cell topRight = field.getCell(topRow, rightCol);
                Cell topCenter = field.getCell(topRow, middleCol);

                bottomRight.getContainedParticles().stream().forEach((other) -> {
                    if (particle.borderToBorderDistance(other) <= r_c) {
                        addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                    }
                });
                middleRight.getContainedParticles().stream().forEach((other) -> {
                    if (particle.borderToBorderDistance(other) <= r_c) {
                        addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                    }
                });
                topRight.getContainedParticles().stream().forEach((other) -> {
                    if (particle.borderToBorderDistance(other) <= r_c) {
                        addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                    }
                });
                topCenter.getContainedParticles().stream().forEach((other) -> {
                    if (particle.borderToBorderDistance(other) <= r_c) {
                        addNeighbourRelationsToMap(map, particle.getId(), other.getId());
                    }
                });
                if (map.get(particle.getId()) == null) {
                    map.put(particle.getId(), new HashSet<>());
                }
            }
            uncheckedParticles.clear();
        }

        // OUTPUT JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("CIM_neighbours.json")) {
            gson.toJson(map, writer);
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }

    }
}
