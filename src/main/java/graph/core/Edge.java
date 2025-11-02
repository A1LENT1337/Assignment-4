package graph.core;

import java.util.Objects;

public class Edge {
    private int from;
    private int to;
    private int weight;

    public Edge(int from, int to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    // Getters
    public int getFrom() { return from; }
    public int getTo() { return to; }
    public int getWeight() { return weight; }

    @Override
    public String toString() {
        return String.format("%d->%d(%d)", from, to, weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return from == edge.from && to == edge.to && weight == edge.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, weight);
    }
}