package graph.dagsp;

import graph.core.Graph;
import graph.metrics.MetricsCollector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class DAGLongestPathTest {

    @Test
    public void testCriticalPath() {
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 1, 3);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 3, 4);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 2);
        graph.addEdge(3, 5, 3);
        graph.addEdge(4, 5, 1);

        DAGLongestPath longestPath = new DAGLongestPath();
        PathResult result = longestPath.findLongestPaths(graph, 0);

        assertEquals(10, result.getCriticalPathLength());
        List<Integer> criticalPath = result.getCriticalPath();
        assertEquals(4, criticalPath.size());
        assertEquals(0, criticalPath.get(0));
        assertEquals(1, criticalPath.get(1));
        assertEquals(3, criticalPath.get(2));
        assertEquals(5, criticalPath.get(3));
    }

    @Test
    public void testAllEqualWeights() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 3, 1);
        graph.addEdge(3, 4, 1);

        DAGLongestPath longestPath = new DAGLongestPath();
        PathResult result = longestPath.findLongestPaths(graph, 0);

        assertEquals(2, result.getCriticalPathLength());
        assertTrue(result.hasPath(2));
        assertTrue(result.hasPath(4));
    }

    @Test
    public void testPathReconstruction() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 3);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 4);

        DAGLongestPath longestPath = new DAGLongestPath();
        PathResult result = longestPath.findLongestPaths(graph, 0);

        List<Integer> path = result.getPath(3);
        assertEquals(3, path.size(), "Path should have 3 vertices");
        assertEquals(0, path.get(0), "Path should start from source");
        assertEquals(3, path.get(2), "Path should end at target");
        assertEquals(7, result.getDistance(3), "Longest path distance should be 7");

        int middleVertex = path.get(1);
        assertTrue(middleVertex == 1 || middleVertex == 2,
                "Middle vertex should be either 1 or 2");
    }

    @Test
    public void testSingleVertexLongestPath() {
        Graph graph = new Graph(1, true);

        DAGLongestPath longestPath = new DAGLongestPath();
        PathResult result = longestPath.findLongestPaths(graph, 0);

        assertEquals(0, result.getDistance(0));
        assertEquals(0, result.getCriticalPathLength());
        assertEquals(1, result.getCriticalPath().size());
        assertEquals(0, result.getCriticalPath().get(0));
    }

    @Test
    public void testMetricsCollection() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 3, 1);
        graph.addEdge(3, 2, 1);

        MetricsCollector metrics = new MetricsCollector();
        DAGLongestPath longestPath = new DAGLongestPath();
        PathResult result = longestPath.findLongestPaths(graph, 0, metrics);

        assertTrue(metrics.getRelaxations() > 0);
        assertTrue(metrics.getEdgeTraversals() > 0);
        assertTrue(metrics.getExecutionTimeNanos() > 0);
    }

    @Test
    public void testUnreachableInLongestPath() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        DAGLongestPath longestPath = new DAGLongestPath();
        PathResult result = longestPath.findLongestPaths(graph, 0);

        assertFalse(result.hasPath(3));
        assertEquals(Integer.MIN_VALUE, result.getDistance(3));
        assertTrue(result.getPath(3).isEmpty());
    }

    @Test
    public void testOverallCriticalPath() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, 3);
        graph.addEdge(2, 3, 4);
        graph.addEdge(0, 4, 10);

        DAGLongestPath longestPath = new DAGLongestPath();
        PathResult result = longestPath.findOverallCriticalPath(graph, new graph.metrics.MetricsCollector());

        assertNotNull(result);
        assertTrue(result.getCriticalPathLength() >= 10);
        assertFalse(result.getCriticalPath().isEmpty());
    }

    @Test
    public void testComplexCriticalPath() {
        Graph graph = new Graph(7, true);
        graph.addEdge(0, 1, 3);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 3, 4);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 2);
        graph.addEdge(3, 5, 3);
        graph.addEdge(4, 6, 5);
        graph.addEdge(5, 6, 2);

        DAGLongestPath longestPath = new DAGLongestPath();
        PathResult result = longestPath.findLongestPaths(graph, 0);

        assertEquals(14, result.getCriticalPathLength());
    }
}