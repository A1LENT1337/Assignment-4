import generator.GraphGenerator;
import graph.core.Graph;
import graph.core.GraphLoader;
import graph.scc.TarjanSCC;
import graph.scc.SCCResult;
import graph.topo.TopologicalSort;
import graph.topo.TopoResult;
import graph.dagsp.DAGShortestPath;
import graph.dagsp.DAGLongestPath;
import graph.dagsp.PathResult;
import graph.metrics.MetricsCollector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static final String[] DATASETS = {
            "small_1.json", "small_2.json", "small_3.json",
            "medium_1.json", "medium_2.json", "medium_3.json",
            "large_1.json", "large_2.json", "large_3.json"
    };

    public static void main(String[] args) {
        System.out.println("🚀 SMART CITY TASK SCHEDULING - STARTING");
        System.out.println("==========================================");

        // ALWAYS generate datasets first
        System.out.println("📊 STEP 1: Generating datasets...");
        generateDatasets();

        // THEN run analysis
        System.out.println("📈 STEP 2: Running analysis...");
        runAnalysis();

        System.out.println("✅ PROGRAM COMPLETED SUCCESSFULLY!");
    }

    private static void generateDatasets() {
        try {
            GraphGenerator generator = new GraphGenerator();
            generator.generateAllDatasets();
            System.out.println("✅ Datasets generated in data/ directory");
        } catch (Exception e) {
            System.err.println("❌ Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runAnalysis() {
        System.out.println("Smart City Task Scheduling - Graph Algorithms Analysis");
        System.out.println("======================================================");

        try {
            CSVWriter csv = new CSVWriter("results/results.csv");
            csv.writeHeader();

            for (String dataset : DATASETS) {
                String filepath = "data/" + dataset;
                if (Files.exists(Paths.get(filepath))) {
                    System.out.println("\nProcessing: " + dataset);
                    processDataset(filepath, csv);
                } else {
                    System.out.println("Warning: Dataset not found: " + filepath);
                }
            }

            csv.close();
            System.out.println("\nAnalysis complete! Results saved to results/results.csv");

        } catch (IOException e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processDataset(String filepath, CSVWriter csv) throws IOException {
        int[] sourceVertex = new int[1];
        sourceVertex[0] = 0; // Default source

        Graph graph = GraphLoader.loadFromJSON(filepath, sourceVertex);
        int source = sourceVertex[0];

        int nodes = graph.getVertexCount();
        int edges = countEdges(graph);
        String datasetName = new File(filepath).getName();

        System.out.println("  Graph: " + nodes + " nodes, " + edges + " edges");

        System.out.println("  Running SCC analysis...");
        MetricsCollector sccMetrics = new MetricsCollector();
        TarjanSCC tarjan = new TarjanSCC();
        SCCResult sccResult = tarjan.findSCCs(graph, sccMetrics);

        csv.writeRow(datasetName, nodes, edges, "SCC", "DFSVisits",
                sccMetrics.getDFSVisits(), sccMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "SCC", "EdgeTraversals",
                sccMetrics.getEdgeTraversals(), sccMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "SCC", "ComponentCount",
                sccResult.getComponentCount(), sccMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "SCC", "ExecutionTime",
                sccMetrics.getExecutionTimeNanos(), sccMetrics.getExecutionTimeNanos());

        System.out.println("  Running topological sort...");
        Graph condensation = sccResult.getCondensationGraph();
        MetricsCollector topoMetrics = new MetricsCollector();
        TopologicalSort topoSort = new TopologicalSort();
        TopoResult topoResult = topoSort.kahnSort(condensation, topoMetrics);

        csv.writeRow(datasetName, nodes, edges, "Topo", "QueueOps",
                topoMetrics.getQueueOperations(), topoMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "Topo", "EdgeTraversals",
                topoMetrics.getEdgeTraversals(), topoMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "Topo", "ExecutionTime",
                topoMetrics.getExecutionTimeNanos(), topoMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "Topo", "IsValidDAG",
                topoResult.isDAG() ? 1 : 0, topoMetrics.getExecutionTimeNanos());

        System.out.println("  Running shortest path on original graph...");
        MetricsCollector shortestMetrics = new MetricsCollector();
        DAGShortestPath shortestPath = new DAGShortestPath();

        PathResult shortestResult;
        if (sccResult.getComponentCount() == nodes) {
            shortestResult = shortestPath.findShortestPaths(graph, source, shortestMetrics);
        } else {
            shortestResult = shortestPath.findShortestPaths(condensation, 0, shortestMetrics);
        }

        csv.writeRow(datasetName, nodes, edges, "ShortestPath", "Relaxations",
                shortestMetrics.getRelaxations(), shortestMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "ShortestPath", "EdgeTraversals",
                shortestMetrics.getEdgeTraversals(), shortestMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "ShortestPath", "ExecutionTime",
                shortestMetrics.getExecutionTimeNanos(), shortestMetrics.getExecutionTimeNanos());

        int reachableShortest = shortestResult.getReachableVertices().size();
        csv.writeRow(datasetName, nodes, edges, "ShortestPath", "ReachableVertices",
                reachableShortest, shortestMetrics.getExecutionTimeNanos());

        double avgShortestDistance = calculateAverageDistance(shortestResult);
        csv.writeRow(datasetName, nodes, edges, "ShortestPath", "AvgDistance",
                (int)(avgShortestDistance * 100), shortestMetrics.getExecutionTimeNanos());

        System.out.println("  Running longest path on original graph...");
        MetricsCollector longestMetrics = new MetricsCollector();
        DAGLongestPath longestPath = new DAGLongestPath();

        PathResult longestResult;
        if (sccResult.getComponentCount() == nodes) {
            longestResult = longestPath.findLongestPaths(graph, source, longestMetrics);
        } else {
            longestResult = longestPath.findLongestPaths(condensation, 0, longestMetrics);
        }

        csv.writeRow(datasetName, nodes, edges, "LongestPath", "Relaxations",
                longestMetrics.getRelaxations(), longestMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "LongestPath", "EdgeTraversals",
                longestMetrics.getEdgeTraversals(), longestMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "LongestPath", "ExecutionTime",
                longestMetrics.getExecutionTimeNanos(), longestMetrics.getExecutionTimeNanos());
        csv.writeRow(datasetName, nodes, edges, "LongestPath", "CriticalPathLength",
                longestResult.getCriticalPathLength(), longestMetrics.getExecutionTimeNanos());

        int reachableLongest = longestResult.getReachableVertices().size();
        csv.writeRow(datasetName, nodes, edges, "LongestPath", "ReachableVertices",
                reachableLongest, longestMetrics.getExecutionTimeNanos());

        csv.writeRow(datasetName, nodes, edges, "LongestPath", "CriticalPathVertices",
                longestResult.getCriticalPath().size(), longestMetrics.getExecutionTimeNanos());

        System.out.println("  Completed: " + datasetName);
        printDatasetSummary(sccResult, topoResult, shortestResult, longestResult);
    }

    private static double calculateAverageDistance(PathResult result) {
        int[] distances = result.getDistances();
        int reachableCount = 0;
        double totalDistance = 0;

        for (int i = 0; i < distances.length; i++) {
            if (result.hasPath(i) && distances[i] != Integer.MAX_VALUE && distances[i] != Integer.MIN_VALUE) {
                totalDistance += distances[i];
                reachableCount++;
            }
        }

        return reachableCount > 0 ? totalDistance / reachableCount : 0;
    }

    private static int countEdges(Graph graph) {
        int edgeCount = 0;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            edgeCount += graph.getNeighbors(i).size();
        }
        return edgeCount;
    }

    private static void printDatasetSummary(SCCResult scc, TopoResult topo,
                                            PathResult shortest, PathResult longest) {
        System.out.println("    SCC Components: " + scc.getComponentCount());
        System.out.println("    Topological Order Valid: " + topo.isDAG());
        System.out.println("    Shortest Path Reachable: " + shortest.getReachableVertices().size());
        System.out.println("    Longest Path Critical Length: " + longest.getCriticalPathLength());
    }

    private static class CSVWriter {
        private PrintWriter writer;

        public CSVWriter(String filepath) throws IOException {
            new File("results").mkdirs();
            this.writer = new PrintWriter(new FileWriter(filepath));
        }

        public void writeHeader() {
            writer.println("Dataset,Nodes,Edges,Algorithm,Metric,Value,TimeNanos");
        }

        public void writeRow(String dataset, int nodes, int edges, String algorithm,
                             String metric, int value, long timeNanos) {
            writer.printf("%s,%d,%d,%s,%s,%d,%d%n",
                    dataset, nodes, edges, algorithm, metric, value, timeNanos);
        }

        public void writeRow(String dataset, int nodes, int edges, String algorithm,
                             String metric, long value, long timeNanos) {
            writer.printf("%s,%d,%d,%s,%s,%d,%d%n",
                    dataset, nodes, edges, algorithm, metric, value, timeNanos);
        }

        public void close() {
            writer.close();
        }
    }
}