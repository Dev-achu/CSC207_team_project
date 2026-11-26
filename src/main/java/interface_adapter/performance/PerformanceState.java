package interface_adapter.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the performance comparison View displays, already formatted as strings.
 */
public class PerformanceState {
    private String portfolioName = "";
    private String benchmarkName = "";
    private String dateRange = "";
    private String portfolioNominalReturn = "0.00%";
    private String portfolioRealReturn = "0.00%";
    private String benchmarkNominalReturn = "0.00%";
    private String benchmarkRealReturn = "0.00%";
    private String inflationRate = "0.00%";
    private boolean beatingBenchmark;
    private boolean beatingInflation;
    private String summary = "";
    private List<String> chartRows = new ArrayList<>();
    private String error = "";

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    /** Returns the formatted nominal portfolio return, e.g. "20.00%". */
    public String getPortfolioNominalReturn() {
        return portfolioNominalReturn;
    }

    public void setPortfolioNominalReturn(String portfolioNominalReturn) {
        this.portfolioNominalReturn = portfolioNominalReturn;
    }

    /** Returns the formatted inflation-adjusted portfolio return, e.g. "16.50%". */
    public String getPortfolioRealReturn() {
        return portfolioRealReturn;
    }

    public void setPortfolioRealReturn(String portfolioRealReturn) {
        this.portfolioRealReturn = portfolioRealReturn;
    }

    /** Returns the formatted nominal benchmark return, e.g. "10.00%". */
    public String getBenchmarkNominalReturn() {
        return benchmarkNominalReturn;
    }

    public void setBenchmarkNominalReturn(String benchmarkNominalReturn) {
        this.benchmarkNominalReturn = benchmarkNominalReturn;
    }

    /** Returns the formatted inflation-adjusted benchmark return, e.g. "6.80%". */
    public String getBenchmarkRealReturn() {
        return benchmarkRealReturn;
    }

    public void setBenchmarkRealReturn(String benchmarkRealReturn) {
        this.benchmarkRealReturn = benchmarkRealReturn;
    }

    /** Returns the formatted CPI inflation rate over the range, e.g. "3.00%". */
    public String getInflationRate() {
        return inflationRate;
    }

    public void setInflationRate(String inflationRate) {
        this.inflationRate = inflationRate;
    }

    public boolean isBeatingBenchmark() {
        return beatingBenchmark;
    }

    public void setBeatingBenchmark(boolean beatingBenchmark) {
        this.beatingBenchmark = beatingBenchmark;
    }

    public boolean isBeatingInflation() {
        return beatingInflation;
    }

    public void setBeatingInflation(boolean beatingInflation) {
        this.beatingInflation = beatingInflation;
    }

    /** Returns a human-readable summary comparing nominal and inflation-adjusted returns. */
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /** Returns one formatted side-by-side comparison line per date point in the chart. */
    public List<String> getChartRows() {
        return chartRows;
    }

    public void setChartRows(List<String> chartRows) {
        this.chartRows = chartRows;
    }

    /** Returns an error message for the last action, or "" if it succeeded. */
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
