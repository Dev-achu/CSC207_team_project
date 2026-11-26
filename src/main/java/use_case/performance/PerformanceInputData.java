package use_case.performance;

import java.time.LocalDate;

/**
 * The input for comparing portfolio performance against a benchmark and inflation:
 * created by the controller and passed to the interactor through the input boundary.
 */
public class PerformanceInputData {
    private final String username;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String benchmarkSymbol;

    public PerformanceInputData(String username, LocalDate startDate,
                                LocalDate endDate, String benchmarkSymbol) {
        this.username = username;
        this.startDate = startDate;
        this.endDate = endDate;
        this.benchmarkSymbol = benchmarkSymbol;
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getBenchmarkSymbol() {
        return benchmarkSymbol;
    }
}
