package generator;

import graph.core.Graph;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GraphGenerator {
    private Random random;
    private Gson gson;

    public GraphGenerator() {
        this.random = new Random(42);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public GraphGenerator(long seed) {
        this.random = new Random(seed);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Graph generateGraph(int n, double edgeFactor, boolean includeCycles, String weightModel) {
        Graph graph = new Graph(n, true, weightModel);

        if (includeCycles) {
            generateMeaningfulCyclicGraph(graph, n, edgeFactor);
        } else {
            generateCleanDAG(graph, n, edgeFactor);
        }

        return graph;
    }

    private void generateMeaningfulCyclicGraph(Graph graph, int n, double edgeFactor) {
        int targetEdges = Math.max(n, (int) (n * edgeFactor));

        createMeaningfulCycle(graph, n, 0, n/3);
        if (n >= 6) {
            createMeaningfulCycle(graph, n, n/3, 2*n/3);
        }

        addConnectingEdges(graph, n);

        int currentEdges = countEdges(graph);
        int maxAttempts = n * n * 2;
        int attempts = 0;

        while (currentEdges < targetEdges && attempts < maxAttempts) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            int weight = random.nextInt(10) + 1;

            if (u != v && !hasEdge(graph, u, v)) {
                graph.addEdge(u, v, weight);
                currentEdges++;
            }
            attempts++;
        }
    }

    private void generateCleanDAG(Graph graph, int n, double edgeFactor) {
        int targetEdges = Math.max(n-1, (int) (n * edgeFactor));
        int edgesAdded = 0;

        int layers = Math.min(5, 2 + n/5);
        List<List<Integer>> layerVertices = new ArrayList<>();

        for (int i = 0; i < layers; i++) {
            layerVertices.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            int layer = random.nextInt(layers);
            layerVertices.get(layer).add(i);
        }

        for (int i = 0; i < layers - 1; i++) {
            List<Integer> currentLayer = layerVertices.get(i);
            List<Integer> nextLayer = layerVertices.get(i + 1);

            if (!currentLayer.isEmpty() && !nextLayer.isEmpty()) {
                for (int u : currentLayer) {
                    int v = nextLayer.get(random.nextInt(nextLayer.size()));
                    int weight = random.nextInt(10) + 1;
                    if (!hasEdge(graph, u, v)) {
                        graph.addEdge(u, v, weight);
                        edgesAdded++;
                    }
                }
            }
        }

        int maxAttempts = n * n;
        int attempts = 0;

        while (edgesAdded < targetEdges && attempts < maxAttempts) {
            int u = random.nextInt(n);
            int v;

            if (u < n - 1) {
                v = u + 1 + random.nextInt(n - u - 1);
            } else {
                v = random.nextInt(n);
            }

            if (v > u && !hasEdge(graph, u, v)) {
                int weight = random.nextInt(10) + 1;
                graph.addEdge(u, v, weight);
                edgesAdded++;
            }
            attempts++;
        }

        if (edgesAdded < targetEdges) {
            for (int u = 0; u < n && edgesAdded < targetEdges; u++) {
                for (int v = u + 1; v < n && edgesAdded < targetEdges; v++) {
                    if (!hasEdge(graph, u, v)) {
                        int weight = random.nextInt(10) + 1;
                        graph.addEdge(u, v, weight);
                        edgesAdded++;
                    }
                }
            }
        }
    }

    private void createMeaningfulCycle(Graph graph, int n, int start, int end) {
        if (end - start < 2) return;

        int cycleSize = Math.min(5, 2 + (end - start) / 2);
        List<Integer> cycleVertices = new ArrayList<>();

        for (int i = 0; i < cycleSize; i++) {
            int vertex;
            int attempts = 0;
            do {
                vertex = start + random.nextInt(Math.min(end - start, n - start));
                attempts++;
            } while (cycleVertices.contains(vertex) && attempts < 10);

            if (vertex < n) {
                cycleVertices.add(vertex);
            }
        }

        for (int i = 0; i < cycleSize; i++) {
            int u = cycleVertices.get(i);
            int v = cycleVertices.get((i + 1) % cycleSize);
            int weight = random.nextInt(10) + 1;
            graph.addEdge(u, v, weight);
        }
    }

    private void addConnectingEdges(Graph graph, int n) {
        // Add some cross connections
        int connections = Math.max(1, n / 4);
        for (int i = 0; i < connections; i++) {
            int u = random.nextInt(n/2); // from first half
            int v = n/2 + random.nextInt(n - n/2); // to second half
            int weight = random.nextInt(10) + 1;

            if (!hasEdge(graph, u, v)) {
                graph.addEdge(u, v, weight);
            }
        }
    }

    private boolean hasEdge(Graph graph, int u, int v) {
        for (graph.core.Edge edge : graph.getNeighbors(u)) {
            if (edge.getTo() == v) {
                return true;
            }
        }
        return false;
    }

    private int countEdges(Graph graph) {
        int count = 0;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            count += graph.getNeighbors(i).size();
        }
        return count;
    }

    public void saveToJSON(Graph g, String filepath, Integer source) throws IOException {
        java.io.File directory = new java.io.File(filepath).getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("directed", g.isDirected());
        jsonObject.addProperty("n", g.getVertexCount());

        JsonArray edgesArray = new JsonArray();
        for (int u = 0; u < g.getVertexCount(); u++) {
            for (graph.core.Edge edge : g.getNeighbors(u)) {
                JsonObject edgeObject = new JsonObject();
                edgeObject.addProperty("u", edge.getFrom());
                edgeObject.addProperty("v", edge.getTo());
                edgeObject.addProperty("w", edge.getWeight());
                edgesArray.add(edgeObject);
            }
        }
        jsonObject.add("edges", edgesArray);

        if (source != null) {
            jsonObject.addProperty("source", source);
        }

        jsonObject.addProperty("weight_model", g.getWeightModel());

        try (FileWriter writer = new FileWriter(filepath)) {
            gson.toJson(jsonObject, writer);
        }
    }

    public void generateAllDatasets() {
        try {
            // Create data directory
            java.io.File dataDir = new java.io.File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            System.out.println("Generating 9 CLEAN datasets for Smart City Task Scheduling...");

            // Small datasets - sparse, meaningful structures
            generateSmallDatasets();
            // Medium datasets - moderate complexity
            generateMediumDatasets();
            // Large datasets - larger but still clean
            generateLargeDatasets();

            System.out.println("All 9 CLEAN datasets generated successfully in data/ directory!");

        } catch (IOException e) {
            System.err.println("Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateSmallDatasets() throws IOException {
        System.out.println("📊 Generating small datasets...");

        // small_1.json: 6 nodes, sparse, 1-2 cycles
        Graph small1 = generateGraph(6, 1.5, true, "edge");
        saveToJSON(small1, "data/small_1.json", 0);
        System.out.println("  ✅ small_1.json created: " + getGraphStatistics(small1));

        // small_2.json: 8 nodes, clean DAG
        Graph small2 = generateGraph(8, 1.8, false, "edge");
        saveToJSON(small2, "data/small_2.json", 0);
        System.out.println("  ✅ small_2.json created: " + getGraphStatistics(small2));

        // small_3.json: 10 nodes, mixed with cycles
        Graph small3 = generateGraph(10, 2.0, true, "edge");
        saveToJSON(small3, "data/small_3.json", 0);
        System.out.println("  ✅ small_3.json created: " + getGraphStatistics(small3));
    }

    private void generateMediumDatasets() throws IOException {
        System.out.println("📊 Generating medium datasets...");

        // medium_1.json: 12 nodes, moderate cycles
        Graph medium1 = generateGraph(12, 2.2, true, "edge");
        saveToJSON(medium1, "data/medium_1.json", 0);
        System.out.println("  ✅ medium_1.json created: " + getGraphStatistics(medium1));

        // medium_2.json: 15 nodes, dense but meaningful
        Graph medium2 = generateGraph(15, 2.5, true, "edge");
        saveToJSON(medium2, "data/medium_2.json", 0);
        System.out.println("  ✅ medium_2.json created: " + getGraphStatistics(medium2));

        // medium_3.json: 20 nodes, clean DAG
        Graph medium3 = generateGraph(20, 2.8, false, "edge");
        saveToJSON(medium3, "data/medium_3.json", 0);
        System.out.println("  ✅ medium_3.json created: " + getGraphStatistics(medium3));
    }

    private void generateLargeDatasets() throws IOException {
        System.out.println("📊 Generating large datasets...");

        // large_1.json: 25 nodes, sparse cycles
        Graph large1 = generateGraph(25, 3.0, true, "edge");
        saveToJSON(large1, "data/large_1.json", 0);
        System.out.println("  ✅ large_1.json created: " + getGraphStatistics(large1));

        // large_2.json: 35 nodes, moderate complexity
        Graph large2 = generateGraph(35, 3.5, true, "edge");
        saveToJSON(large2, "data/large_2.json", 0);
        System.out.println("  ✅ large_2.json created: " + getGraphStatistics(large2));

        // large_3.json: 50 nodes, clean DAG
        Graph large3 = generateGraph(50, 4.0, false, "edge");
        saveToJSON(large3, "data/large_3.json", 0);
        System.out.println("  ✅ large_3.json created: " + getGraphStatistics(large3));
    }

    public String getGraphStatistics(Graph graph) {
        int vertices = graph.getVertexCount();
        int edges = countEdges(graph);
        double density = (double) edges / vertices;

        return String.format("Vertices: %d, Edges: %d, Density: %.2f", vertices, edges, density);
    }
}