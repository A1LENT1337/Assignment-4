package graph.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GraphLoader {
    private static final Gson gson = new Gson();

    public static Graph loadFromJSON(String filepath) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(filepath)));
        JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);

        int vertices = jsonObject.get("n").getAsInt();
        boolean directed = jsonObject.get("directed").getAsBoolean();
        String weightModel = jsonObject.get("weight_model").getAsString();

        Graph graph = new Graph(vertices, directed, weightModel);

        JsonArray edges = jsonObject.getAsJsonArray("edges");
        for (JsonElement edgeElement : edges) {
            JsonObject edge = edgeElement.getAsJsonObject();
            int from = edge.get("u").getAsInt();
            int to = edge.get("v").getAsInt();
            int weight = edge.get("w").getAsInt();

            graph.addEdge(from, to, weight);
        }

        return graph;
    }

    public static Graph loadFromJSON(String filepath, int[] sourceVertex) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(filepath)));
        JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);

        int vertices = jsonObject.get("n").getAsInt();
        boolean directed = jsonObject.get("directed").getAsBoolean();
        String weightModel = jsonObject.get("weight_model").getAsString();

        if (jsonObject.has("source") && sourceVertex != null && sourceVertex.length > 0) {
            sourceVertex[0] = jsonObject.get("source").getAsInt();
        }

        Graph graph = new Graph(vertices, directed, weightModel);

        JsonArray edges = jsonObject.getAsJsonArray("edges");
        for (JsonElement edgeElement : edges) {
            JsonObject edge = edgeElement.getAsJsonObject();
            int from = edge.get("u").getAsInt();
            int to = edge.get("v").getAsInt();
            int weight = edge.get("w").getAsInt();

            graph.addEdge(from, to, weight);
        }

        return graph;
    }
}