package ar.edu.itba.ss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cell {
    private Pair<Integer> position;
    private Set<Particle> containedParticles = new HashSet<>();

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

    public Set<Particle> getContainedParticles() {
        return containedParticles;
    }
}
