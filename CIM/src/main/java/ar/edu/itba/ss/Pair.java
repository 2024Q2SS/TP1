package ar.edu.itba.ss;

import java.util.Objects;

public class Pair<K> {
    private K first;
    private K second;

    public Pair(K first, K second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public K getSecond() {
        return second;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public void setSecond(K second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair<?> pair = (Pair<?>) obj;
        return first.equals(pair.first) && second.equals(pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
