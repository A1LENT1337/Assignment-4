package graph.metrics;

public interface Metrics {
    void incrementDFSVisits();
    void incrementEdgeTraversals();
    void incrementRelaxations();
    void incrementQueueOperations();
    void startTimer();
    void stopTimer();
    long getExecutionTimeNanos();
    int getDFSVisits();
    int getEdgeTraversals();
    int getRelaxations();
    int getQueueOperations();
    void reset();
}