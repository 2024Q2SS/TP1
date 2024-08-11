package ar.edu.itba.ss;

public class Coordinates {
    private Pair<Double> coordinates;

    public Coordinates(double x, double y) {
        coordinates = new Pair<>(x, y);
    }

    public double getX() {
        return coordinates.getFirst();
    }

    public double getY() {
        return coordinates.getSecond();
    }

    public Double euclideanDistance(Coordinates other) {
        return Math.sqrt(Math.pow(this.getX() - other.getX(), 2) + Math.pow(this.getY() - other.getY(), 2));
    }
}
