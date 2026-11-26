package use_case.performance;

import entity.Benchmark;
import entity.PricePoint;

import java.time.LocalDate;
import java.util.List;

/**
 * The market data the Performance vs. Benchmark & Inflation use case needs:
 * historical prices for portfolio holdings and for market index benchmarks
 * (such as S&P 500 or TSX). Kept small on purpose (Interface Segregation);
 * the shared stock data gateway can implement this interface.
 */
public interface PerformanceStockDataAccessInterface {

    /**
     * Returns the historical price series for a market benchmark over the
     * given date range.
     *
     * @param benchmarkSymbol the benchmark symbol or name (e.g. "S&P 500", "SPY", "TSX")
     * @param startDate       the start of the date range (inclusive)
     * @param endDate         the end of the date range (inclusive)
     * @return the benchmark series, or null if benchmark data is unavailable
     */
    Benchmark getBenchmarkSeries(String benchmarkSymbol, LocalDate startDate, LocalDate endDate);

    /**
     * Returns the historical closing prices for a held security over the
     * given date range.
     *
     * @param ticker    the security ticker symbol in upper case
     * @param startDate the start of the date range (inclusive)
     * @param endDate   the end of the date range (inclusive)
     * @return the price points in the range, or null / empty list if unavailable
     */
    List<PricePoint> getPriceHistory(String ticker, LocalDate startDate, LocalDate endDate);
}
