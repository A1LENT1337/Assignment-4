package graph.dagsp;

import graph.core.Graph;
import graph.metrics.MetricsCollector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class DAGShortestPathTest {

    @Test
    public void testBasicShortestPath() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 3);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 1);

        DAGShortestPath shortestPath = new DAGShortestPath();
        PathResult result = shortestPath.findShortestPaths(graph, 0);

        assertEquals(0, result.getDistance(0));
        assertEquals(5, result.getDistance(1));
        assertEquals(3, result.getDistance(2));
        assertEquals(4, result.getDistance(3));
    }

    @Test
    public void testPathReconstruction() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, 3);
        graph.addEdge(0, 3, 1);
        graph.addEdge(3, 4, 2);
        graph.addEdge(4, 2, 1);

        DAGShortestPath shortestPath = new DAGShortestPath();
        PathResult result = shortestPath.findShortestPaths(graph, 0);

        List<Integer> path = result.getPath(2);
        assertEquals(4, path.size());
        assertEquals(0, path.get(0));
        assertEquals(3, path.get(1));
        assertEquals(4, path.get(2));
        assertEquals(2, path.get(3));
        assertEquals(4, result.getDistance(2));
    }

    @Test
    public void testUnreachableVertex() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        DAGShortestPath shortestPath = new DAGShortestPath();
        PathResult result = shortestPath.findShortestPaths(graph, 0);

        assertFalse(result.hasPath(3));
        assertEquals(Integer.MAX_VALUE, result.getDistance(3));
        assertTrue(result.getPath(3).isEmpty());
    }

    @Test
    public void testSingleVertex() {
        Graph graph = new Graph(1, true);

        DAGShortestPath shortestPath = new DAGShortestPath();
        PathResult result = shortestPath.findShortestPaths(graph, 0);

        assertEquals(0, result.getDistance(0));
        assertEquals(1, result.getPath(0).size());
        assertEquals(0, result.getPath(0).get(0));
    }

    @Test
    public void testMetricsCollection() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 3, 1);
        graph.addEdge(3, 2, 1);

        MetricsCollector metrics = new MetricsCollector();
        DAGShortestPath shortestPath = new DAGShortestPath();
        PathResult result = shortestPath.findShortestPaths(graph, 0, metrics);

        assertTrue(metrics.getRelaxations() > 0);
        assertTrue(metrics.getEdgeTraversals() > 0);
        assertTrue(metrics.getExecutionTimeNanos() > 0);
    }

    @Test
    public void testMultiplePaths() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 4);
        graph.addEdge(3, 4, 2);

        DAGShortestPath shortestPath = new DAGShortestPath();
        PathResult result = shortestPath.findShortestPaths(graph, 0);

        assertEquals(8, result.getDistance(4));
        assertTrue(result.hasPath(4));
    }

    @Test
    public void testNegativeWeights() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 3, -3);
        graph.addEdge(2, 3, 4);

        DAGShortestPath shortestPath = new DAGShortestPath();
        PathResult result = shortestPath.findShortestPaths(graph, 0);

        assertEquals(2, result.getDistance(3),
                "Should take the path with negative weight: 0->1->3 = 5 + (-3) = 2");

        List<Integer> path = result.getPath(3);
        assertEquals(3, path.size(), "Path should have 3 vertices");
        assertEquals(0, path.get(0), "Path should start from source");
        assertEquals(3, path.get(2), "Path should end at target");
    }
}