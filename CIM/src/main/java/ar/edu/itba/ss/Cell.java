package ar.edu.itba.ss;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    private Pair<Integer> position;
    private List<Particle> containedParticles = new ArrayList<>();

    public Cell(Integer col, Integer row) {
        this.position = new Pair<>(col, row);
    }

    public void removeParticle(Particle particle) {
        containedParticles.remove(particle);
    }

    public void addParticle(Particle particle) {
        containedParticles.add(particle);
    }

    public void removeParticles() {
        containedParticles.clear();
    }

    public void addParticles(List<Particle> particles) {
        containedParticles.addAll(particles);
    }

    public Integer getCol() {
        return position.getFirst();
    }

    public Integer getRow() {
        return position.getSecond();
    }

    public List<Particle> getContainedParticles() {
        return containedParticles;
    }
}
