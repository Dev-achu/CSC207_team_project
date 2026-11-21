package interface_adapter.performance;

import use_case.performance.PerformanceInputBoundary;
import use_case.performance.PerformanceInputData;

import java.time.LocalDate;

/**
 * Receives performance comparison requests from the View, packages them as
 * input data, and starts the Performance vs. Benchmark & Inflation use case.
 */
public class PerformanceController {
    private final PerformanceInputBoundary performanceInteractor;

    public PerformanceController(PerformanceInputBoundary performanceInteractor) {
        this.performanceInteractor = performanceInteractor;
    }

    /**
     * Compares the user's portfolio return against a market benchmark and
     * inflation over the given date range.
     *
     * @param username        the logged-in user's username
     * @param startDate       the start of the comparison date range
     * @param endDate         the end of the comparison date range
     * @param benchmarkSymbol the selected benchmark symbol or name (e.g. "S&P 500", "TSX")
     */
    public void execute(String username, LocalDate startDate,
                        LocalDate endDate, String benchmarkSymbol) {
        PerformanceInputData inputData = new PerformanceInputData(
                username, startDate, endDate, benchmarkSymbol);
        performanceInteractor.execute(inputData);
    }
}
