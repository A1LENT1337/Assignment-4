package graph.dagsp;

import graph.core.Graph;
import graph.core.Edge;
import graph.metrics.Metrics;
import graph.topo.TopologicalSort;
import graph.topo.TopoResult;
import java.util.*;

public class DAGLongestPath {

    public PathResult findLongestPaths(Graph dag, int source, Metrics metrics) {
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
            throw new IllegalArgumentException("Graph must be a DAG for longest path calculation");
        }

        List<Integer> topoOrder = topoResult.getOrder();

        int[] dist = new int[n];
        int[] parent = new int[n];

        Arrays.fill(dist, Integer.MIN_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        for (int u : topoOrder) {
            if (dist[u] != Integer.MIN_VALUE) {
                for (Edge edge : dag.getNeighbors(u)) {
                    metrics.incrementEdgeTraversals();
                    int v = edge.getTo();
                    int weight = edge.getWeight();

                    metrics.incrementRelaxations();
                    if (dist[v] < dist[u] + weight) {
                        dist[v] = dist[u] + weight;
                        parent[v] = u;
                    }
                }
            }
        }

        metrics.stopTimer();

        List<Integer> criticalPath = findCriticalPath(dist, parent, source);
        int criticalPathLength = criticalPath.isEmpty() ? 0 : dist[criticalPath.get(criticalPath.size() - 1)];

        PathResult result = new PathResult(dist, parent, source, PathResult.PathType.LONGEST);
        result.setCriticalPath(criticalPath);
        result.setCriticalPathLength(criticalPathLength);

        return result;
    }

    private List<Integer> findCriticalPath(int[] dist, int[] parent, int source) {
        int n = dist.length;
        int maxDist = Integer.MIN_VALUE;
        int target = source;

        for (int i = 0; i < n; i++) {
            if (dist[i] != Integer.MIN_VALUE && dist[i] > maxDist) {
                maxDist = dist[i];
                target = i;
            }
        }

        return reconstructPath(parent, source, target);
    }

    private List<Integer> reconstructPath(int[] parent, int source, int target) {
        List<Integer> path = new ArrayList<>();

        if (parent[target] == -1 && target != source) {
            return path;
        }

        int current = target;
        while (current != -1) {
            path.add(current);
            current = parent[current];
            if (current == source) {
                path.add(source);
                break;
            }
        }

        Collections.reverse(path);
        return path;
    }

    public PathResult findOverallCriticalPath(Graph dag, Metrics metrics) {
        if (metrics == null) {
            metrics = new graph.metrics.MetricsCollector();
        }

        metrics.startTimer();

        int n = dag.getVertexCount();
        List<Integer> bestCriticalPath = new ArrayList<>();
        int bestLength = Integer.MIN_VALUE;
        PathResult bestResult = null;

        for (int source = 0; source < n; source++) {
            PathResult result = findLongestPaths(dag, source, metrics);
            if (result.getCriticalPathLength() > bestLength) {
                bestLength = result.getCriticalPathLength();
                bestCriticalPath = result.getCriticalPath();
                bestResult = result;
            }
        }

        metrics.stopTimer();

        if (bestResult != null) {
            bestResult.setCriticalPath(bestCriticalPath);
            bestResult.setCriticalPathLength(bestLength);
        }

        return bestResult;
    }

    public PathResult findLongestPaths(Graph dag, int source) {
        return findLongestPaths(dag, source, new graph.metrics.MetricsCollector());
    }

    public PathResult findOverallCriticalPath(Graph dag) {
        return findOverallCriticalPath(dag, new graph.metrics.MetricsCollector());
    }
}