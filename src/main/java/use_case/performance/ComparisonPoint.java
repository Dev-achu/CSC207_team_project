package use_case.performance;

import java.time.LocalDate;

/**
 * One point in the side-by-side performance chart: cumulative portfolio return,
 * benchmark return, and inflation from the start of the range up to this date.
 * Part of the output data, so the presenter never needs to touch entities.
 */
public class ComparisonPoint {
    private final LocalDate date;
    private final double portfolioReturn;
    private final double benchmarkReturn;
    private final double inflationRate;

    public ComparisonPoint(LocalDate date, double portfolioReturn,
                           double benchmarkReturn, double inflationRate) {
        this.date = date;
        this.portfolioReturn = portfolioReturn;
        this.benchmarkReturn = benchmarkReturn;
        this.inflationRate = inflationRate;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getPortfolioReturn() {
        return portfolioReturn;
    }

    public double getBenchmarkReturn() {
        return benchmarkReturn;
    }

    public double getInflationRate() {
        return inflationRate;
    }
}
