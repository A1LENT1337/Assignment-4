package graph.topo;

import graph.scc.SCCResult;
import graph.core.Graph;
import graph.core.Edge;
import java.util.*;

public class TopoResult {
    private List<Integer> order; // topological order of components
    private boolean isDAG; // should be true for condensation graphs

    public TopoResult(List<Integer> order, boolean isDAG) {
        this.order = new ArrayList<>(order);
        this.isDAG = isDAG;
    }

    // Getters
    public List<Integer> getOrder() {
        return Collections.unmodifiableList(order);
    }

    public boolean isDAG() {
        return isDAG;
    }

    public List<Integer> getOriginalTaskOrder(SCCResult sccResult) {
        if (!isDAG) {
            throw new IllegalStateException("Cannot get task order from non-DAG");
        }

        List<Integer> taskOrder = new ArrayList<>();

        for (int componentId : order) {
            List<Integer> componentVertices = sccResult.getComponentVertices(componentId);
            taskOrder.addAll(componentVertices);
        }

        return taskOrder;
    }

    public int getPositionInOrder(int vertex) {
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i) == vertex) {
                return i;
            }
        }
        return -1;
    }

    public boolean isValidOrder(Graph dag) {
        Map<Integer, Integer> positionMap = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            positionMap.put(order.get(i), i);
        }

        for (int u = 0; u < dag.getVertexCount(); u++) {
            for (Edge edge : dag.getNeighbors(u)) {
                int v = edge.getTo();
                if (positionMap.get(u) >= positionMap.get(v)) {
                    return false; // u should come before v
                }
            }
        }

        return true;
    }

    public String getStatistics() {
        return String.format(
                "Topological Order Statistics:\n" +
                        "  Order Length: %d\n" +
                        "  Is Valid DAG: %s\n" +
                        "  First Element: %d\n" +
                        "  Last Element: %d",
                order.size(), isDAG,
                order.isEmpty() ? "N/A" : order.get(0),
                order.isEmpty() ? "N/A" : order.get(order.size() - 1)
        );
    }

    @Override
    public String toString() {
        return "Topological Order: " + order + " (Valid DAG: " + isDAG + ")";
    }
}