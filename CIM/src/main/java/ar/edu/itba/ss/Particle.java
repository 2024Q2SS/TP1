package ar.edu.itba.ss;

public class Particle {
    private Coordinates coordinates;
    private Integer id;
    private Cell cell;

    public Particle(Integer id, Coordinates coordinates) {
        this.coordinates = coordinates;
        this.id = id;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Cell getCell() {
        return cell;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Integer getId() {
        return id;
    }
}
