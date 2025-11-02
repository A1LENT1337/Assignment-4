package graph.topo;

import graph.core.Graph;
import graph.core.Edge;
import graph.metrics.Metrics;
import graph.scc.SCCResult;
import java.util.*;

public class TopologicalSort {

    public TopoResult kahnSort(Graph dag, Metrics metrics) {
        if (metrics == null) {
            metrics = new graph.metrics.MetricsCollector();
        }

        metrics.startTimer();

        int n = dag.getVertexCount();
        int[] inDegree = new int[n];

        for (int u = 0; u < n; u++) {
            for (Edge edge : dag.getNeighbors(u)) {
                metrics.incrementEdgeTraversals();
                int v = edge.getTo();
                inDegree[v]++;
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int u = 0; u < n; u++) {
            if (inDegree[u] == 0) {
                queue.offer(u);
                metrics.incrementQueueOperations();
            }
        }

        List<Integer> topologicalOrder = new ArrayList<>();
        int visitedCount = 0;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            metrics.incrementQueueOperations();

            topologicalOrder.add(u);
            visitedCount++;

            for (Edge edge : dag.getNeighbors(u)) {
                metrics.incrementEdgeTraversals();
                int v = edge.getTo();
                inDegree[v]--;

                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.incrementQueueOperations();
                }
            }
        }

        metrics.stopTimer();

        boolean isDAG = (visitedCount == n);

        return new TopoResult(topologicalOrder, isDAG);
    }

    public TopoResult dfsSort(Graph dag, Metrics metrics) {
        if (metrics == null) {
            metrics = new graph.metrics.MetricsCollector();
        }

        metrics.startTimer();

        int n = dag.getVertexCount();
        boolean[] visited = new boolean[n];
        boolean[] inStack = new boolean[n];
        List<Integer> order = new ArrayList<>();

        for (int u = 0; u < n; u++) {
            if (!visited[u]) {
                if (!dfsVisit(dag, u, visited, inStack, order, metrics)) {

                    metrics.stopTimer();
                    return new TopoResult(new ArrayList<>(), false);
                }
            }
        }

        metrics.stopTimer();

        Collections.reverse(order);
        return new TopoResult(order, true);
    }

    private boolean dfsVisit(Graph dag, int u, boolean[] visited, boolean[] inStack,
                             List<Integer> order, Metrics metrics) {
        metrics.incrementDFSVisits();

        if (inStack[u]) {
            return false;
        }

        if (visited[u]) {
            return true;
        }

        visited[u] = true;
        inStack[u] = true;

        for (Edge edge : dag.getNeighbors(u)) {
            metrics.incrementEdgeTraversals();
            int v = edge.getTo();
            if (!dfsVisit(dag, v, visited, inStack, order, metrics)) {
                return false;
            }
        }

        inStack[u] = false;
        order.add(u);
        return true;
    }

    public TopoResult kahnSort(Graph dag) {
        return kahnSort(dag, new graph.metrics.MetricsCollector());
    }

    public TopoResult dfsSort(Graph dag) {
        return dfsSort(dag, new graph.metrics.MetricsCollector());
    }
}