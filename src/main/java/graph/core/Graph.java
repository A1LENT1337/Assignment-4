package graph.core;

import java.util.*;

public class Graph {
    private int n; // number of vertices
    private List<List<Edge>> adjacencyList;
    private boolean directed;
    private String weightModel; // "edge" or "node"

    // Constructors
    public Graph(int n, boolean directed) {
        this(n, directed, "edge");
    }

    public Graph(int n, boolean directed, String weightModel) {
        this.n = n;
        this.directed = directed;
        this.weightModel = weightModel;
        this.adjacencyList = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    // Methods
    public void addEdge(int u, int v, int weight) {
        if (u < 0 || u >= n || v < 0 || v >= n) {
            throw new IllegalArgumentException("Vertex index out of bounds: " + u + " -> " + v);
        }

        adjacencyList.get(u).add(new Edge(u, v, weight));

        // If undirected, add reverse edge
        if (!directed) {
            adjacencyList.get(v).add(new Edge(v, u, weight));
        }
    }

    public List<Edge> getNeighbors(int u) {
        if (u < 0 || u >= n) {
            throw new IllegalArgumentException("Vertex index out of bounds: " + u);
        }
        return Collections.unmodifiableList(adjacencyList.get(u));
    }

    public int getVertexCount() {
        return n;
    }

    public boolean isDirected() {
        return directed;
    }

    public String getWeightModel() {
        return weightModel;
    }

    public List<Edge> getAllEdges() {
        List<Edge> allEdges = new ArrayList<>();
        for (List<Edge> edges : adjacencyList) {
            allEdges.addAll(edges);
        }
        return allEdges;
    }

    public Graph getReverse() {
        if (!directed) {
            return this; // Undirected graph is its own reverse
        }

        Graph reverse = new Graph(n, directed, weightModel);
        for (int u = 0; u < n; u++) {
            for (Edge edge : adjacencyList.get(u)) {
                reverse.addEdge(edge.getTo(), edge.getFrom(), edge.getWeight());
            }
        }
        return reverse;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph (vertices: ").append(n)
                .append(", directed: ").append(directed)
                .append(", weight model: ").append(weightModel)
                .append(")\n");

        for (int u = 0; u < n; u++) {
            sb.append(u).append(": ");
            for (Edge edge : adjacencyList.get(u)) {
                sb.append(edge).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}