package use_case.performance;

/**
 * Input boundary for the Performance vs. Benchmark & Inflation use case (UC4).
 * Implemented by the interactor and called by the controller.
 */
public interface PerformanceInputBoundary {

    /**
     * Computes the portfolio's nominal and inflation-adjusted return over a date range,
     * compares it against the selected benchmark and CPI inflation, and reports the
     * result through the output boundary.
     *
     * @param inputData the selected portfolio owner, date range, and benchmark
     */
    void execute(PerformanceInputData inputData);
}
