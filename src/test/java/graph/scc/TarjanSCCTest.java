package graph.scc;

import graph.core.Graph;
import graph.metrics.MetricsCollector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class TarjanSCCTest {

    @Test
    public void testSimpleCycle() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1); // This creates the cycle

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCCs(graph);

        assertEquals(1, result.getComponentCount(),
                "3-cycle should have exactly 1 SCC");
        assertEquals(3, result.getComponentVertices(0).size(),
                "The single SCC should contain all 3 vertices");
        assertTrue(result.areInSameComponent(0, 1), "0 and 1 should be in same component");
        assertTrue(result.areInSameComponent(1, 2), "1 and 2 should be in same component");
        assertTrue(result.areInSameComponent(0, 2), "0 and 2 should be in same component");
    }

    @Test
    public void testMultipleSCCs() {
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(4, 5, 1);

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCCs(graph);

        assertEquals(3, result.getComponentCount(),
                "Should find 3 SCCs: {0,1,2}, {3,4}, {5}");

        assertTrue(result.areInSameComponent(0, 1), "0 and 1 should be in same component");
        assertTrue(result.areInSameComponent(0, 2), "0 and 2 should be in same component");
        assertTrue(result.areInSameComponent(3, 4), "3 and 4 should be in same component");
        assertFalse(result.areInSameComponent(0, 3), "0 and 3 should be in different components");
        assertFalse(result.areInSameComponent(0, 5), "0 and 5 should be in different components");

        List<Integer> componentSizes = result.getComponentSizes();
        assertTrue(componentSizes.contains(3), "Should have one component with 3 vertices");
        assertTrue(componentSizes.contains(2), "Should have one component with 2 vertices");
        assertTrue(componentSizes.contains(1), "Should have one component with 1 vertex");
    }

    @Test
    public void testDAG() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 3, 1);
        graph.addEdge(3, 2, 1);

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCCs(graph);

        assertEquals(4, result.getComponentCount(),
                "DAG should have 4 SCCs (one per vertex)");

        Set<Integer> allVertices = new HashSet<>();
        for (int i = 0; i < result.getComponentCount(); i++) {
            List<Integer> component = result.getComponentVertices(i);
            assertEquals(1, component.size(),
                    "Each component should have exactly 1 vertex in a DAG");
            allVertices.addAll(component);
        }

        assertEquals(4, allVertices.size());
        assertTrue(allVertices.contains(0));
        assertTrue(allVertices.contains(1));
        assertTrue(allVertices.contains(2));
        assertTrue(allVertices.contains(3));

        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                assertFalse(result.areInSameComponent(i, j),
                        "In a DAG, different vertices should be in different components");
            }
        }
    }

    @Test
    public void testSingleVertex() {
        Graph graph = new Graph(1, true);

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCCs(graph);

        assertEquals(1, result.getComponentCount());
        assertEquals(1, result.getComponentVertices(0).size());
        assertEquals(0, result.getComponentVertices(0).get(0));
    }

    @Test
    public void testDisconnectedComponents() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCCs(graph);

        assertEquals(3, result.getComponentCount());
        assertTrue(result.areInSameComponent(0, 1));
        assertTrue(result.areInSameComponent(2, 3));
        assertFalse(result.areInSameComponent(0, 2));
        assertFalse(result.areInSameComponent(0, 4));
    }

    @Test
    public void testEmptyGraph() {
        Graph graph = new Graph(0, true);

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCCs(graph);

        assertEquals(0, result.getComponentCount(),
                "Empty graph should have 0 components");
        assertTrue(result.getComponents().isEmpty(),
                "Components list should be empty");
    }

    @Test
    public void testMetricsCollection() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(1, 3, 1);

        MetricsCollector metrics = new MetricsCollector();
        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCCs(graph, metrics);

        assertTrue(metrics.getDFSVisits() > 0);
        assertTrue(metrics.getEdgeTraversals() > 0);
        assertTrue(metrics.getExecutionTimeNanos() > 0);
        assertEquals(4, metrics.getDFSVisits()); // Should visit all 4 vertices
    }

    @Test
    public void testCondensationGraphStructure() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(3, 4, 1);

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCCs(graph);

        Graph condensation = result.getCondensationGraph();
        assertEquals(3, condensation.getVertexCount(),
                "Condensation should have 3 vertices: 2 cycles + 1 single vertex");

        boolean hasEdgesBetweenComponents = false;
        for (int i = 0; i < condensation.getVertexCount(); i++) {
            if (condensation.getNeighbors(i).size() > 0) {
                hasEdgesBetweenComponents = true;
                break;
            }
        }

        assertTrue(hasEdgesBetweenComponents,
                "Condensation graph should have edges between components");

        graph.topo.TopologicalSort topoSort = new graph.topo.TopologicalSort();
        graph.topo.TopoResult topoResult = topoSort.kahnSort(condensation);
        assertTrue(topoResult.isDAG(), "Condensation graph should be a DAG");
    }
}