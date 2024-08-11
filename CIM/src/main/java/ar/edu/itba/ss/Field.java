package ar.edu.itba.ss;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Field {
    private final double width;
    private final double height;
    private Map<Pair<Integer>, Cell> cells = new HashMap<>();

    public Field(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void addCell(Cell cell) {
        cells.put(new Pair<>(cell.getRow(), cell.getCol()), cell);
    }

    public Cell getCell(Integer x, Integer y) {
        return cells.get(new Pair<>(x, y));
    }

    public Collection<Cell> getCells() {
        return cells.values();
    }
}
