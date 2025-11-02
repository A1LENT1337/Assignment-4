package graph.dagsp;

import graph.core.Graph;
import graph.core.Edge;
import graph.metrics.Metrics;
import graph.topo.TopologicalSort;
import graph.topo.TopoResult;
import java.util.*;


public class DAGShortestPath {

    public PathResult findShortestPaths(Graph dag, int source, Metrics metrics) {
        if (metrics == null) {
            metrics = new graph.metrics.MetricsCollector();
        }

        metrics.startTimer();

        int n = dag.getVertexCount();

        if (source < 0 || source >= n) {
            throw new IllegalArgumentException("Source vertex out of bounds: " + source);
        }

        TopologicalSort topoSort = new TopologicalSort();
        TopoResult topoResult = topoSort.kahnSort(dag, metrics);

        if (!topoResult.isDAG()) {
            throw new IllegalArgumentException("Graph must be a DAG for shortest path calculation");
        }

        List<Integer> topoOrder = topoResult.getOrder();

        int[] dist = new int[n];
        int[] parent = new int[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        for (int u : topoOrder) {
            if (dist[u] != Integer.MAX_VALUE) {
                for (Edge edge : dag.getNeighbors(u)) {
                    metrics.incrementEdgeTraversals();
                    int v = edge.getTo();
                    int weight = edge.getWeight();

                    metrics.incrementRelaxations();
                    if (dist[v] > dist[u] + weight) {
                        dist[v] = dist[u] + weight;
                        parent[v] = u;
                    }
                }
            }
        }

        metrics.stopTimer();

        return new PathResult(dist, parent, source, PathResult.PathType.SHORTEST);
    }

    public PathResult findShortestPaths(Graph dag, int source) {
        return findShortestPaths(dag, source, new graph.metrics.MetricsCollector());
    }

    public PathResult findShortestPath(Graph dag, int source, int target, Metrics metrics) {
        PathResult result = findShortestPaths(dag, source, metrics);

        List<Integer> path = result.getPath(target);
        int distance = result.getDistance(target);

        return new PathResult(
                new int[]{distance},
                new int[]{source},
                source,
                PathResult.PathType.SHORTEST
        );
    }
}