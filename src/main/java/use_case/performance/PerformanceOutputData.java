package use_case.performance;

import java.time.LocalDate;
import java.util.List;

/**
 * The result of comparing a portfolio's performance against a market benchmark
 * and CPI inflation over a date range. Created by the interactor and passed to
 * the presenter.
 */
public class PerformanceOutputData {
    private final String portfolioName;
    private final String benchmarkName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final double portfolioNominalReturn;
    private final double portfolioRealReturn;
    private final double benchmarkNominalReturn;
    private final double benchmarkRealReturn;
    private final double inflationRate;
    private final boolean beatingBenchmark;
    private final boolean beatingInflation;
    private final List<ComparisonPoint> series;

    /**
     * Creates the performance comparison output data.
     *
     * @param portfolioName          the portfolio's display name
     * @param benchmarkName          the benchmark's display name (e.g. "S&P 500", "TSX")
     * @param startDate              the start of the comparison range
     * @param endDate                the end of the comparison range
     * @param portfolioNominalReturn the portfolio's simple total return (e.g. 0.15 for +15%)
     * @param portfolioRealReturn    the portfolio's inflation-adjusted return
     * @param benchmarkNominalReturn the benchmark's simple total return
     * @param benchmarkRealReturn    the benchmark's inflation-adjusted return
     * @param inflationRate          the cumulative CPI inflation rate over the range
     * @param beatingBenchmark       true if the portfolio beat the benchmark
     * @param beatingInflation       true if the portfolio beat inflation (positive real return)
     * @param series                 side-by-side cumulative return points across the date range
     */
    public PerformanceOutputData(String portfolioName, String benchmarkName,
                                 LocalDate startDate, LocalDate endDate,
                                 double portfolioNominalReturn, double portfolioRealReturn,
                                 double benchmarkNominalReturn, double benchmarkRealReturn,
                                 double inflationRate, boolean beatingBenchmark,
                                 boolean beatingInflation, List<ComparisonPoint> series) {
        this.portfolioName = portfolioName;
        this.benchmarkName = benchmarkName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.portfolioNominalReturn = portfolioNominalReturn;
        this.portfolioRealReturn = portfolioRealReturn;
        this.benchmarkNominalReturn = benchmarkNominalReturn;
        this.benchmarkRealReturn = benchmarkRealReturn;
        this.inflationRate = inflationRate;
        this.beatingBenchmark = beatingBenchmark;
        this.beatingInflation = beatingInflation;
        this.series = series;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getPortfolioNominalReturn() {
        return portfolioNominalReturn;
    }

    public double getPortfolioRealReturn() {
        return portfolioRealReturn;
    }

    public double getBenchmarkNominalReturn() {
        return benchmarkNominalReturn;
    }

    public double getBenchmarkRealReturn() {
        return benchmarkRealReturn;
    }

    public double getInflationRate() {
        return inflationRate;
    }

    public boolean isBeatingBenchmark() {
        return beatingBenchmark;
    }

    public boolean isBeatingInflation() {
        return beatingInflation;
    }

    public List<ComparisonPoint> getSeries() {
        return series;
    }
}
