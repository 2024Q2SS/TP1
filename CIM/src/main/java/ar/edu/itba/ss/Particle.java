package ar.edu.itba.ss;

public class Particle {
    private Coordinates coordinates;
    private Integer id;
    private Double radius;
    private Cell cell;

    public Particle(Integer id, Double radius) {
        this.id = id;
        this.radius = radius;
    }

    public Particle(Integer id, Coordinates coordinates, Double radius) {
        this.coordinates = coordinates;
        this.id = id;
        this.radius = radius;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCoordinate(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Integer getId() {
        return id;
    }

    public Double getRadius() {
        return radius;
    }

    public Double borderToBorderDistance(Particle other) {
        return this.coordinates.euclideanDistance(other.getCoordinates()) - this.radius - other.getRadius();
    }
}
