package use_case.performance;

import entity.Benchmark;
import entity.Portfolio;
import entity.PricePoint;
import entity.Transaction;
import entity.TransactionType;
import use_case.InflationDataGateway;
import use_case.PortfolioRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UC4 Performance vs. Benchmark & Inflation: computes a portfolio's simple total
 * return (nominal and inflation-adjusted) over a date range and compares it side
 * by side against a selected market benchmark (e.g. S&P 500, TSX) and CPI inflation.
 *
 * <p>Error cases: invalid date range, portfolio has no history in the range,
 * benchmark data unavailable, or CPI data unavailable.</p>
 */
public class PerformanceInteractor implements PerformanceInputBoundary {
    private final PortfolioRepository portfolioRepository;
    private final PerformanceStockDataAccessInterface stockDataAccess;
    private final InflationDataGateway inflationDataGateway;
    private final PerformanceOutputBoundary presenter;

    public PerformanceInteractor(PortfolioRepository portfolioRepository,
                                 PerformanceStockDataAccessInterface stockDataAccess,
                                 InflationDataGateway inflationDataGateway,
                                 PerformanceOutputBoundary presenter) {
        this.portfolioRepository = portfolioRepository;
        this.stockDataAccess = stockDataAccess;
        this.inflationDataGateway = inflationDataGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(PerformanceInputData inputData) {
        // 1. Validate the date range and benchmark input.
        LocalDate startDate = inputData.getStartDate();
        LocalDate endDate = inputData.getEndDate();
        if (startDate == null || endDate == null) {
            presenter.prepareFailView("Start date and end date are required.");
            return;
        }
        if (!startDate.isBefore(endDate)) {
            presenter.prepareFailView("Start date must be before end date.");
            return;
        }
        String benchmarkSymbol = inputData.getBenchmarkSymbol() == null
                ? "" : inputData.getBenchmarkSymbol().trim();
        if (benchmarkSymbol.isEmpty()) {
            presenter.prepareFailView("Please select a benchmark.");
            return;
        }

        // 2. Load the user's portfolio and check that it has history in the range.
        Portfolio portfolio = portfolioRepository.findByOwner(inputData.getUsername());
        if (portfolio == null || !hasHistoryInRange(portfolio, startDate, endDate)) {
            presenter.prepareFailView("Portfolio has no history in the selected date range.");
            return;
        }

        // 3. Fetch benchmark and CPI series over the same range.
        Benchmark benchmark = stockDataAccess.getBenchmarkSeries(benchmarkSymbol, startDate, endDate);
        if (benchmark == null || !benchmark.hasDataInRange(startDate, endDate)) {
            presenter.prepareFailView("Benchmark data is unavailable for " + benchmarkSymbol + ".");
            return;
        }

        Benchmark cpi = inflationDataGateway.getCpiSeries(startDate, endDate);
        if (cpi == null || !cpi.hasDataInRange(startDate, endDate)) {
            presenter.prepareFailView("CPI data is unavailable for the selected date range.");
            return;
        }

        // 4. Load price history for all securities held or traded up to endDate.
        Map<String, List<PricePoint>> securityPrices = loadSecurityPriceHistories(
                portfolio, startDate, endDate);

        // 5. Compute nominal returns, inflation rate, and inflation-adjusted (real) returns.
        double startPortfolioValue = computePortfolioValueAt(portfolio, startDate, securityPrices);
        double portfolioNominalReturn = computePortfolioCumulativeReturn(
                portfolio, startDate, endDate, startPortfolioValue, securityPrices);
        double benchmarkNominalReturn = benchmark.getTotalReturn(startDate, endDate);
        double inflationRate = cpi.getTotalReturn(startDate, endDate);

        double portfolioRealReturn = computeRealReturn(portfolioNominalReturn, inflationRate);
        double benchmarkRealReturn = computeRealReturn(benchmarkNominalReturn, inflationRate);

        boolean beatingBenchmark = portfolioNominalReturn > benchmarkNominalReturn;
        boolean beatingInflation = portfolioNominalReturn > inflationRate;

        // 6. Build the side-by-side comparison series across the date range.
        List<ComparisonPoint> series = buildComparisonSeries(
                portfolio, benchmark, cpi, startDate, endDate, startPortfolioValue, securityPrices);

        presenter.prepareSuccessView(new PerformanceOutputData(
                portfolio.getName(),
                benchmark.getName(),
                startDate,
                endDate,
                portfolioNominalReturn,
                portfolioRealReturn,
                benchmarkNominalReturn,
                benchmarkRealReturn,
                inflationRate,
                beatingBenchmark,
                beatingInflation,
                series));
    }

    /**
     * Checks whether the portfolio holds any shares entering {@code startDate}
     * or has any BUY transaction during {@code [startDate, endDate]}.
     */
    private boolean hasHistoryInRange(Portfolio portfolio, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> quantitiesAtStart = getQuantitiesAt(portfolio, startDate);
        for (double qty : quantitiesAtStart.values()) {
            if (qty > 0) {
                return true;
            }
        }
        for (Transaction t : portfolio.getTransactions()) {
            if (!t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate)
                    && t.getType() == TransactionType.BUY) {
                return true;
            }
        }
        return false;
    }

    private Map<String, List<PricePoint>> loadSecurityPriceHistories(
            Portfolio portfolio, LocalDate startDate, LocalDate endDate) {
        Map<String, List<PricePoint>> histories = new HashMap<>();
        for (Transaction t : portfolio.getTransactions()) {
            if (!t.getDate().isAfter(endDate)) {
                String ticker = t.getSecurity().getTicker();
                if (!histories.containsKey(ticker)) {
                    List<PricePoint> raw = stockDataAccess.getPriceHistory(ticker, startDate, endDate);
                    List<PricePoint> sorted = new ArrayList<>();
                    if (raw != null) {
                        sorted.addAll(raw);
                        sorted.sort(Comparator.comparing(PricePoint::getDate));
                    }
                    histories.put(ticker, sorted);
                }
            }
        }
        return histories;
    }

    /**
     * Computes the simple total return of the portfolio from {@code startDate} up to {@code targetDate}.
     * Uses {@code (endingWealth - baseCapital) / baseCapital}, where {@code baseCapital} is the
     * portfolio's market value at {@code startDate} plus any purchases made after {@code startDate}
     * up to {@code targetDate}, and {@code endingWealth} is the portfolio's market value at
     * {@code targetDate} plus any sales proceeds after {@code startDate} up to {@code targetDate}.
     */
    private double computePortfolioCumulativeReturn(
            Portfolio portfolio, LocalDate startDate, LocalDate targetDate,
            double startPortfolioValue, Map<String, List<PricePoint>> securityPrices) {
        double buysAfterStart = 0;
        double sellsAfterStart = 0;
        for (Transaction t : portfolio.getTransactions()) {
            if (t.getDate().isAfter(startDate) && !t.getDate().isAfter(targetDate)) {
                if (t.getType() == TransactionType.BUY) {
                    buysAfterStart += t.getQuantity() * t.getPrice();
                } else {
                    sellsAfterStart += t.getQuantity() * t.getPrice();
                }
            }
        }
        double baseCapital = startPortfolioValue + buysAfterStart;
        if (baseCapital <= 0) {
            return 0;
        }
        double currentPortfolioValue = computePortfolioValueAt(portfolio, targetDate, securityPrices);
        double endingWealth = currentPortfolioValue + sellsAfterStart;
        return (endingWealth - baseCapital) / baseCapital;
    }

    private double computePortfolioValueAt(
            Portfolio portfolio, LocalDate date, Map<String, List<PricePoint>> securityPrices) {
        Map<String, Double> quantities = getQuantitiesAt(portfolio, date);
        double total = 0;
        for (Map.Entry<String, Double> entry : quantities.entrySet()) {
            double qty = entry.getValue();
            if (qty > 0) {
                double price = getSecurityPriceAt(portfolio, entry.getKey(), date, securityPrices);
                total += qty * price;
            }
        }
        return total;
    }

    private Map<String, Double> getQuantitiesAt(Portfolio portfolio, LocalDate date) {
        Map<String, Double> quantities = new LinkedHashMap<>();
        for (Transaction t : portfolio.getTransactions()) {
            if (!t.getDate().isAfter(date)) {
                String ticker = t.getSecurity().getTicker();
                double current = quantities.getOrDefault(ticker, 0.0);
                if (t.getType() == TransactionType.BUY) {
                    quantities.put(ticker, current + t.getQuantity());
                } else {
                    quantities.put(ticker, Math.max(0.0, current - t.getQuantity()));
                }
            }
        }
        return quantities;
    }

    private double getSecurityPriceAt(
            Portfolio portfolio, String ticker, LocalDate date,
            Map<String, List<PricePoint>> securityPrices) {
        List<PricePoint> history = securityPrices.get(ticker);
        if (history != null && !history.isEmpty()) {
            PricePoint chosen = history.get(0);
            for (PricePoint point : history) {
                if (!point.getDate().isAfter(date)) {
                    chosen = point;
                } else {
                    break;
                }
            }
            return chosen.getClose();
        }
        // Fallback to the latest transaction price on or before the given date.
        Transaction chosen = null;
        for (Transaction t : portfolio.getTransactions()) {
            if (t.getSecurity().getTicker().equals(ticker)) {
                if (chosen == null || (!t.getDate().isAfter(date)
                        && !t.getDate().isBefore(chosen.getDate()))) {
                    chosen = t;
                }
            }
        }
        return chosen == null ? 0 : chosen.getPrice();
    }

    private static double computeRealReturn(double nominalReturn, double inflationRate) {
        return (1.0 + nominalReturn) / (1.0 + inflationRate) - 1.0;
    }

    private List<ComparisonPoint> buildComparisonSeries(
            Portfolio portfolio, Benchmark benchmark, Benchmark cpi,
            LocalDate startDate, LocalDate endDate,
            double startPortfolioValue, Map<String, List<PricePoint>> securityPrices) {
        List<PricePoint> benchmarkPoints = benchmark.getPointsInRange(startDate, endDate);
        List<PricePoint> cpiPoints = cpi.getPointsInRange(startDate, endDate);
        double benchmarkStart = benchmarkPoints.get(0).getClose();
        double cpiStart = cpiPoints.get(0).getClose();

        List<ComparisonPoint> series = new ArrayList<>();
        for (PricePoint bmPoint : benchmarkPoints) {
            LocalDate date = bmPoint.getDate();
            double portfolioReturn = computePortfolioCumulativeReturn(
                    portfolio, startDate, date, startPortfolioValue, securityPrices);
            double benchmarkReturn = (bmPoint.getClose() - benchmarkStart) / benchmarkStart;
            double cpiReturn = (cpi.getCloseOnOrBefore(date) - cpiStart) / cpiStart;
            series.add(new ComparisonPoint(date, portfolioReturn, benchmarkReturn, cpiReturn));
        }
        return series;
    }
}
