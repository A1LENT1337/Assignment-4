package graph.scc;

import graph.core.Graph;
import graph.core.Edge;
import graph.metrics.Metrics;
import java.util.*;

public class TarjanSCC {
    private Graph graph;
    private Metrics metrics;
    private int time;
    private int[] disc;
    private int[] low;
    private boolean[] onStack;
    private Stack<Integer> stack;
    private List<List<Integer>> sccs;
    private Map<Integer, Integer> vertexToComponent;

    public SCCResult findSCCs(Graph g, Metrics m) {
        this.graph = g;
        this.metrics = m;
        this.time = 0;
        int n = graph.getVertexCount();

        this.disc = new int[n];
        this.low = new int[n];
        this.onStack = new boolean[n];
        this.stack = new Stack<>();
        this.sccs = new ArrayList<>();
        this.vertexToComponent = new HashMap<>();

        Arrays.fill(disc, -1); // -1 means undiscovered

        metrics.startTimer();

        for (int u = 0; u < n; u++) {
            if (disc[u] == -1) {
                dfs(u);
            }
        }

        metrics.stopTimer();

        Graph condensationGraph = buildCondensationGraph();

        return new SCCResult(sccs, vertexToComponent, condensationGraph);
    }

    private void dfs(int u) {
        metrics.incrementDFSVisits();

        disc[u] = time;
        low[u] = time;
        time++;
        stack.push(u);
        onStack[u] = true;

        for (Edge edge : graph.getNeighbors(u)) {
            metrics.incrementEdgeTraversals();
            int v = edge.getTo();

            if (disc[v] == -1) {
                // v is not visited
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // v is in stack and hence in current SCC
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        if (low[u] == disc[u]) {
            List<Integer> component = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                component.add(w);
                vertexToComponent.put(w, sccs.size());
            } while (w != u);
            sccs.add(component);
        }
    }

    private Graph buildCondensationGraph() {
        int componentCount = sccs.size();
        Graph condensation = new Graph(componentCount, true, graph.getWeightModel());

        Set<String> addedEdges = new HashSet<>();

        for (int u = 0; u < graph.getVertexCount(); u++) {
            int compU = vertexToComponent.get(u);

            for (Edge edge : graph.getNeighbors(u)) {
                metrics.incrementEdgeTraversals();
                int v = edge.getTo();
                int compV = vertexToComponent.get(v);

                if (compU != compV) {
                    String edgeKey = compU + "->" + compV;
                    if (!addedEdges.contains(edgeKey)) {

                        condensation.addEdge(compU, compV, edge.getWeight());
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }

    public SCCResult findSCCs(Graph g) {
        graph.metrics.MetricsCollector metrics = new graph.metrics.MetricsCollector();
        return findSCCs(g, metrics);
    }
}