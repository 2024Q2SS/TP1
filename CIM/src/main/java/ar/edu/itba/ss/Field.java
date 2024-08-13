package ar.edu.itba.ss;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Field {
    private final Double width;
    private final Double height;
    private Map<Pair<Integer>, Cell> cells = new HashMap<>();

    public Field(Double width, Double height) {
        this.width = width;
        this.height = height;
    }

    public Double getWidth() {
        return width;
    }

    public Double getHeight() {
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
