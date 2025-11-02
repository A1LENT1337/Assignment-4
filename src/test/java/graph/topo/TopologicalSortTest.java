package graph.topo;

import graph.core.Graph;
import graph.metrics.MetricsCollector;
import graph.scc.SCCResult;
import graph.scc.TarjanSCC;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class TopologicalSortTest {

    @Test
    public void testLinearDAG() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);

        TopologicalSort topoSort = new TopologicalSort();
        TopoResult result = topoSort.kahnSort(graph);

        assertTrue(result.isDAG());
        assertEquals(4, result.getOrder().size());
        assertTrue(result.isValidOrder(graph));

        List<Integer> order = result.getOrder();
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(1) < order.indexOf(2));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    public void testDiamondDAG() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);

        TopologicalSort topoSort = new TopologicalSort();
        TopoResult result = topoSort.kahnSort(graph);

        assertTrue(result.isDAG());
        assertEquals(4, result.getOrder().size());
        assertTrue(result.isValidOrder(graph));

        List<Integer> order = result.getOrder();
        assertEquals(0, order.get(0));
        assertEquals(3, order.get(3));
    }

    @Test
    public void testWithSCCCondensation() {
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(4, 5, 1);

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult sccResult = tarjan.findSCCs(graph);

        Graph condensation = sccResult.getCondensationGraph();
        TopologicalSort topoSort = new TopologicalSort();
        TopoResult topoResult = topoSort.kahnSort(condensation);

        assertTrue(topoResult.isDAG());
        assertEquals(3, topoResult.getOrder().size());
        assertTrue(topoResult.isValidOrder(condensation));

        List<Integer> order = topoResult.getOrder();
        int comp1 = sccResult.getComponentId(0);
        int comp2 = sccResult.getComponentId(3);
        int comp3 = sccResult.getComponentId(5);

        assertTrue(order.indexOf(comp1) < order.indexOf(comp2));
        assertTrue(order.indexOf(comp2) < order.indexOf(comp3));
    }

    @Test
    public void testCycleDetection() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        TopologicalSort topoSort = new TopologicalSort();
        TopoResult result = topoSort.kahnSort(graph);

        assertFalse(result.isDAG());
        assertTrue(result.getOrder().size() < 3,
                "Cycle should result in fewer than 3 vertices in topological order");
    }

    @Test
    public void testSingleVertexDAG() {
        Graph graph = new Graph(1, true);

        TopologicalSort topoSort = new TopologicalSort();
        TopoResult result = topoSort.kahnSort(graph);

        assertTrue(result.isDAG());
        assertEquals(1, result.getOrder().size());
        assertEquals(0, result.getOrder().get(0));
    }

    @Test
    public void testMetricsCollection() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 3, 1);

        MetricsCollector metrics = new MetricsCollector();
        TopologicalSort topoSort = new TopologicalSort();
        TopoResult result = topoSort.kahnSort(graph, metrics);

        assertTrue(metrics.getQueueOperations() > 0);
        assertTrue(metrics.getEdgeTraversals() > 0);
        assertTrue(metrics.getExecutionTimeNanos() > 0);
    }

    @Test
    public void testOriginalTaskOrder() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(3, 4, 1);

        TarjanSCC tarjan = new TarjanSCC();
        SCCResult sccResult = tarjan.findSCCs(graph);

        Graph condensation = sccResult.getCondensationGraph();
        TopologicalSort topoSort = new TopologicalSort();
        TopoResult topoResult = topoSort.kahnSort(condensation);

        List<Integer> taskOrder = topoResult.getOriginalTaskOrder(sccResult);

        assertEquals(5, taskOrder.size());
        for (int i = 0; i < 5; i++) {
            assertTrue(taskOrder.contains(i));
        }
    }

    @Test
    public void testDFSvsKahnConsistency() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 3, 1);
        graph.addEdge(3, 4, 1);

        TopologicalSort topoSort = new TopologicalSort();
        TopoResult kahnResult = topoSort.kahnSort(graph);
        TopoResult dfsResult = topoSort.dfsSort(graph, new graph.metrics.MetricsCollector());

        assertTrue(kahnResult.isDAG());
        assertTrue(dfsResult.isDAG());
        assertEquals(5, kahnResult.getOrder().size());
        assertEquals(5, dfsResult.getOrder().size());
        assertTrue(kahnResult.isValidOrder(graph));
        assertTrue(dfsResult.isValidOrder(graph));
    }
}