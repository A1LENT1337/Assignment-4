package graph.dagsp;

import java.util.*;

public class PathResult {
    public enum PathType {
        SHORTEST, LONGEST
    }

    private int[] distances;
    private int[] parent;
    private int source;
    private PathType pathType;
    private List<Integer> criticalPath;
    private int criticalPathLength;

    public PathResult(int[] distances, int[] parent, int source, PathType pathType) {
        this.distances = Arrays.copyOf(distances, distances.length);
        this.parent = Arrays.copyOf(parent, parent.length);
        this.source = source;
        this.pathType = pathType;
        this.criticalPath = new ArrayList<>();
        this.criticalPathLength = 0;
    }

    // Getters
    public int getDistance(int vertex) {
        if (vertex < 0 || vertex >= distances.length) {
            throw new IllegalArgumentException("Vertex index out of bounds: " + vertex);
        }
        return distances[vertex];
    }

    public int[] getDistances() {
        return Arrays.copyOf(distances, distances.length);
    }

    public int getSource() {
        return source;
    }

    public PathType getPathType() {
        return pathType;
    }

    public List<Integer> getCriticalPath() {
        return Collections.unmodifiableList(criticalPath);
    }

    public int getCriticalPathLength() {
        return criticalPathLength;
    }

    // Setters for critical path (used by longest path algorithm)
    public void setCriticalPath(List<Integer> criticalPath) {
        this.criticalPath = new ArrayList<>(criticalPath);
    }

    public void setCriticalPathLength(int criticalPathLength) {
        this.criticalPathLength = criticalPathLength;
    }

    public List<Integer> getPath(int target) {
        if (target < 0 || target >= parent.length) {
            throw new IllegalArgumentException("Target vertex out of bounds: " + target);
        }

        List<Integer> path = new ArrayList<>();

        if (distances[target] == Integer.MAX_VALUE || distances[target] == Integer.MIN_VALUE) {
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

    public boolean hasPath(int target) {
        if (target < 0 || target >= distances.length) {
            return false;
        }
        return distances[target] != Integer.MAX_VALUE &&
                distances[target] != Integer.MIN_VALUE;
    }

    public List<Integer> getReachableVertices() {
        List<Integer> reachable = new ArrayList<>();
        for (int i = 0; i < distances.length; i++) {
            if (hasPath(i)) {
                reachable.add(i);
            }
        }
        return reachable;
    }

    public String getStatistics() {
        int reachableCount = getReachableVertices().size();
        int totalVertices = distances.length;

        StringBuilder stats = new StringBuilder();
        stats.append(String.format(
                "Path Result Statistics (%s Path):\n",
                pathType == PathType.SHORTEST ? "Shortest" : "Longest"
        ));
        stats.append(String.format("  Source Vertex: %d\n", source));
        stats.append(String.format("  Reachable Vertices: %d/%d\n", reachableCount, totalVertices));

        if (pathType == PathType.LONGEST && !criticalPath.isEmpty()) {
            stats.append(String.format("  Critical Path Length: %d\n", criticalPathLength));
            stats.append(String.format("  Critical Path: %s\n", criticalPath));
        }

        return stats.toString();
    }

    @Override
    public String toString() {
        return String.format(
                "PathResult{type=%s, source=%d, reachable=%d/%d}",
                pathType, source, getReachableVertices().size(), distances.length
        );
    }
}