package graph.scc;

import graph.core.Graph;
import java.util.*;

public class SCCResult {
    private List<List<Integer>> components;
    private Map<Integer, Integer> vertexToComponent;
    private Graph condensationGraph;

    public SCCResult(List<List<Integer>> components,
                     Map<Integer, Integer> vertexToComponent,
                     Graph condensationGraph) {
        this.components = new ArrayList<>(components);
        this.vertexToComponent = new HashMap<>(vertexToComponent);
        this.condensationGraph = condensationGraph;
    }

    // Getters
    public List<List<Integer>> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public Map<Integer, Integer> getVertexToComponent() {
        return Collections.unmodifiableMap(vertexToComponent);
    }

    public Graph getCondensationGraph() {
        return condensationGraph;
    }

    public int getComponentCount() {
        return components.size();
    }

    public List<Integer> getComponentSizes() {
        List<Integer> sizes = new ArrayList<>();
        for (List<Integer> component : components) {
            sizes.add(component.size());
        }
        return sizes;
    }

    public int getComponentId(int vertex) {
        return vertexToComponent.get(vertex);
    }

    public List<Integer> getComponentVertices(int componentId) {
        if (componentId < 0 || componentId >= components.size()) {
            throw new IllegalArgumentException("Invalid component ID: " + componentId);
        }
        return Collections.unmodifiableList(components.get(componentId));
    }

    public boolean areInSameComponent(int u, int v) {
        return vertexToComponent.get(u).equals(vertexToComponent.get(v));
    }

    public String getStatistics() {
        List<Integer> sizes = getComponentSizes();
        int maxSize = Collections.max(sizes);
        int minSize = Collections.min(sizes);
        double avgSize = sizes.stream().mapToInt(Integer::intValue).average().orElse(0);

        return String.format(
                "SCC Statistics:\n" +
                        "  Total Components: %d\n" +
                        "  Largest Component: %d vertices\n" +
                        "  Smallest Component: %d vertices\n" +
                        "  Average Component Size: %.2f vertices",
                getComponentCount(), maxSize, minSize, avgSize
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Strongly Connected Components (").append(getComponentCount()).append(" components):\n");

        for (int i = 0; i < components.size(); i++) {
            sb.append("  Component ").append(i).append(": ").append(components.get(i)).append("\n");
        }

        return sb.toString();
    }
}