package interface_adapter.performance;

import use_case.performance.ComparisonPoint;
import use_case.performance.PerformanceOutputBoundary;
import use_case.performance.PerformanceOutputData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Turns the performance comparison output data into display strings and updates
 * the {@link PerformanceViewModel}.
 */
public class PerformancePresenter implements PerformanceOutputBoundary {
    private final PerformanceViewModel performanceViewModel;

    public PerformancePresenter(PerformanceViewModel performanceViewModel) {
        this.performanceViewModel = performanceViewModel;
    }

    @Override
    public void prepareSuccessView(PerformanceOutputData outputData) {
        PerformanceState state = performanceViewModel.getState();

        String portfolioNom = formatPercent(outputData.getPortfolioNominalReturn());
        String portfolioReal = formatPercent(outputData.getPortfolioRealReturn());
        String benchmarkNom = formatPercent(outputData.getBenchmarkNominalReturn());
        String benchmarkReal = formatPercent(outputData.getBenchmarkRealReturn());
        String inflation = formatPercent(outputData.getInflationRate());

        state.setPortfolioName(outputData.getPortfolioName());
        state.setBenchmarkName(outputData.getBenchmarkName());
        state.setDateRange(outputData.getStartDate() + " to " + outputData.getEndDate());
        state.setPortfolioNominalReturn(portfolioNom);
        state.setPortfolioRealReturn(portfolioReal);
        state.setBenchmarkNominalReturn(benchmarkNom);
        state.setBenchmarkRealReturn(benchmarkReal);
        state.setInflationRate(inflation);
        state.setBeatingBenchmark(outputData.isBeatingBenchmark());
        state.setBeatingInflation(outputData.isBeatingInflation());

        String benchmarkVerdict = outputData.isBeatingBenchmark()
                ? "beat " + outputData.getBenchmarkName()
                : "trailed " + outputData.getBenchmarkName();
        String inflationVerdict = outputData.isBeatingInflation()
                ? "beat inflation"
                : "trailed inflation";
        state.setSummary(String.format(Locale.CANADA,
                "%s returned %s nominal (%s inflation-adjusted) vs. %s at %s (%s inflation-adjusted) "
                        + "and CPI inflation of %s: %s and %s.",
                outputData.getPortfolioName(), portfolioNom, portfolioReal,
                outputData.getBenchmarkName(), benchmarkNom, benchmarkReal,
                inflation, benchmarkVerdict, inflationVerdict));

        List<String> rows = new ArrayList<>();
        for (ComparisonPoint point : outputData.getSeries()) {
            rows.add(String.format(Locale.CANADA,
                    "%s | Portfolio: %s | %s: %s | CPI: %s",
                    point.getDate(),
                    formatPercent(point.getPortfolioReturn()),
                    outputData.getBenchmarkName(),
                    formatPercent(point.getBenchmarkReturn()),
                    formatPercent(point.getInflationRate())));
        }
        state.setChartRows(rows);
        state.setError("");

        performanceViewModel.setState(state);
        performanceViewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        PerformanceState state = performanceViewModel.getState();
        state.setError(errorMessage);
        performanceViewModel.setState(state);
        performanceViewModel.firePropertyChanged();
    }

    private static String formatPercent(double fraction) {
        return String.format(Locale.CANADA, "%.2f%%", fraction * 100);
    }
}
