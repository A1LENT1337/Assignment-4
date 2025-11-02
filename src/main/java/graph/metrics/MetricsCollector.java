package graph.metrics;

public class MetricsCollector implements Metrics {
    private int dfsVisits;
    private int edgeTraversals;
    private int relaxations;
    private int queueOperations;
    private long startTime;
    private long endTime;
    private boolean timerRunning;

    public MetricsCollector() {
        reset();
    }

    @Override
    public synchronized void incrementDFSVisits() {
        dfsVisits++;
    }

    @Override
    public synchronized void incrementEdgeTraversals() {
        edgeTraversals++;
    }

    @Override
    public synchronized void incrementRelaxations() {
        relaxations++;
    }

    @Override
    public synchronized void incrementQueueOperations() {
        queueOperations++;
    }

    @Override
    public synchronized void startTimer() {
        if (!timerRunning) {
            startTime = System.nanoTime();
            timerRunning = true;
        }
    }

    @Override
    public synchronized void stopTimer() {
        if (timerRunning) {
            endTime = System.nanoTime();
            timerRunning = false;
        }
    }

    @Override
    public synchronized long getExecutionTimeNanos() {
        if (timerRunning) {
            return System.nanoTime() - startTime;
        }
        return endTime - startTime;
    }

    @Override
    public synchronized int getDFSVisits() {
        return dfsVisits;
    }

    @Override
    public synchronized int getEdgeTraversals() {
        return edgeTraversals;
    }

    @Override
    public synchronized int getRelaxations() {
        return relaxations;
    }

    @Override
    public synchronized int getQueueOperations() {
        return queueOperations;
    }

    @Override
    public synchronized void reset() {
        dfsVisits = 0;
        edgeTraversals = 0;
        relaxations = 0;
        queueOperations = 0;
        startTime = 0;
        endTime = 0;
        timerRunning = false;
    }

    public synchronized String getReport() {
        long executionTimeMs = getExecutionTimeNanos() / 1_000_000;
        return String.format(
                "Metrics Report:\n" +
                        "  Execution Time: %d ms\n" +
                        "  DFS Visits: %d\n" +
                        "  Edge Traversals: %d\n" +
                        "  Relaxations: %d\n" +
                        "  Queue Operations: %d",
                executionTimeMs, dfsVisits, edgeTraversals, relaxations, queueOperations
        );
    }

    public synchronized String toCSV() {
        long executionTimeMs = getExecutionTimeNanos() / 1_000_000;
        return String.format("%d,%d,%d,%d,%d",
                executionTimeMs, dfsVisits, edgeTraversals, relaxations, queueOperations);
    }
}