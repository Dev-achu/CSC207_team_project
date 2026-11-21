package use_case.performance;

/**
 * Output boundary for the Performance vs. Benchmark & Inflation use case (UC4).
 * Implemented by the presenter and called by the interactor.
 */
public interface PerformanceOutputBoundary {

    /**
     * Called when the performance comparison was computed successfully.
     *
     * @param outputData the nominal and inflation-adjusted returns and chart series
     */
    void prepareSuccessView(PerformanceOutputData outputData);

    /**
     * Called when the performance comparison could not be computed.
     *
     * @param errorMessage a message explaining what went wrong
     */
    void prepareFailView(String errorMessage);
}
