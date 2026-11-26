package use_case.performance;

import data_access.InMemoryInflationDataGateway;
import data_access.InMemoryPortfolioRepository;
import entity.AssetClass;
import entity.Benchmark;
import entity.Portfolio;
import entity.PricePoint;
import entity.Security;
import entity.Transaction;
import entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceInteractorTest {
    private static final double DELTA = 1e-9;
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate MID = LocalDate.of(2026, 3, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 1);

    private final Security apple = new Security("AAPL", "Apple Inc.", AssetClass.STOCK);
    private final Security bonds = new Security("BND", "Vanguard Total Bond Market ETF", AssetClass.BOND);

    private InMemoryPortfolioRepository portfolioRepository;
    private StubPerformanceStockData stockData;
    private InMemoryInflationDataGateway inflationGateway;

    /** A fake market data source for security and benchmark price histories. */
    private static class StubPerformanceStockData implements PerformanceStockDataAccessInterface {
        private final Map<String, Benchmark> benchmarks = new HashMap<>();
        private final Map<String, List<PricePoint>> securityHistories = new HashMap<>();

        void addBenchmark(String key, Benchmark benchmark) {
            benchmarks.put(key.toUpperCase(), benchmark);
        }

        void addSecurityHistory(Security security, List<PricePoint> points) {
            securityHistories.put(security.getTicker(), points);
        }

        @Override
        public Benchmark getBenchmarkSeries(String benchmarkSymbol, LocalDate startDate, LocalDate endDate) {
            return benchmarks.get(benchmarkSymbol.trim().toUpperCase());
        }

        @Override
        public List<PricePoint> getPriceHistory(String ticker, LocalDate startDate, LocalDate endDate) {
            List<PricePoint> points = securityHistories.get(ticker);
            if (points == null) {
                return null;
            }
            List<PricePoint> inRange = new ArrayList<>();
            for (PricePoint p : points) {
                if (!p.getDate().isBefore(startDate) && !p.getDate().isAfter(endDate)) {
                    inRange.add(p);
                }
            }
            return inRange;
        }
    }

    /** Remembers what the interactor sent, so tests can inspect it. */
    private static class RecordingPresenter implements PerformanceOutputBoundary {
        PerformanceOutputData successData;
        String failMessage;

        @Override
        public void prepareSuccessView(PerformanceOutputData outputData) {
            this.successData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            this.failMessage = errorMessage;
        }
    }

    @BeforeEach
    void setUp() {
        portfolioRepository = new InMemoryPortfolioRepository();
        stockData = new StubPerformanceStockData();
        inflationGateway = new InMemoryInflationDataGateway();

        // S&P 500 grows +10% (4000 -> 4200 -> 4400)
        Benchmark sp500 = new Benchmark("S&P 500", "S&P 500", List.of(
                new PricePoint(START, 4000.0),
                new PricePoint(MID, 4200.0),
                new PricePoint(END, 4400.0)));
        stockData.addBenchmark("S&P 500", sp500);

        // TSX grows +5% (20000 -> 20500 -> 21000)
        Benchmark tsx = new Benchmark("TSX", "TSX", List.of(
                new PricePoint(START, 20000.0),
                new PricePoint(MID, 20500.0),
                new PricePoint(END, 21000.0)));
        stockData.addBenchmark("TSX", tsx);

        // CPI grows +3% (150.0 -> 151.5 -> 154.5)
        inflationGateway.addCpiPoint(START, 150.0);
        inflationGateway.addCpiPoint(MID, 151.5);
        inflationGateway.addCpiPoint(END, 154.5);

        // AAPL grows +20% (100 -> 110 -> 120)
        stockData.addSecurityHistory(apple, List.of(
                new PricePoint(apple, START, 100.0),
                new PricePoint(apple, MID, 110.0),
                new PricePoint(apple, END, 120.0)));

        // BND grows +2% (50 -> 50.5 -> 51)
        stockData.addSecurityHistory(bonds, List.of(
                new PricePoint(bonds, START, 50.0),
                new PricePoint(bonds, MID, 50.5),
                new PricePoint(bonds, END, 51.0)));
    }

    private RecordingPresenter run(String username, LocalDate startDate,
                                   LocalDate endDate, String benchmark) {
        RecordingPresenter presenter = new RecordingPresenter();
        PerformanceInteractor interactor = new PerformanceInteractor(
                portfolioRepository, stockData, inflationGateway, presenter);
        interactor.execute(new PerformanceInputData(username, startDate, endDate, benchmark));
        return presenter;
    }

    @Test
    void computesNominalAndRealReturnsWhenBeatingBenchmarkAndInflation() {
        Portfolio portfolio = new Portfolio("p1", "achu", "Growth Portfolio");
        portfolio.addTransaction(new Transaction("t1", apple, TransactionType.BUY, 10, 100.0, START));
        portfolioRepository.save(portfolio);

        RecordingPresenter presenter = run("achu", START, END, "S&P 500");

        assertNull(presenter.failMessage);
        assertNotNull(presenter.successData);
        assertEquals("Growth Portfolio", presenter.successData.getPortfolioName());
        assertEquals("S&P 500", presenter.successData.getBenchmarkName());
        assertEquals(0.20, presenter.successData.getPortfolioNominalReturn(), DELTA);
        assertEquals(0.10, presenter.successData.getBenchmarkNominalReturn(), DELTA);
        assertEquals(0.03, presenter.successData.getInflationRate(), DELTA);

        // Real return: (1 + 0.20) / (1 + 0.03) - 1
        double expectedPortfolioReal = 1.20 / 1.03 - 1.0;
        double expectedBenchmarkReal = 1.10 / 1.03 - 1.0;
        assertEquals(expectedPortfolioReal, presenter.successData.getPortfolioRealReturn(), DELTA);
        assertEquals(expectedBenchmarkReal, presenter.successData.getBenchmarkRealReturn(), DELTA);
        assertTrue(presenter.successData.isBeatingBenchmark());
        assertTrue(presenter.successData.isBeatingInflation());

        List<ComparisonPoint> series = presenter.successData.getSeries();
        assertEquals(3, series.size());
        assertEquals(0.0, series.get(0).getPortfolioReturn(), DELTA);
        assertEquals(0.10, series.get(1).getPortfolioReturn(), DELTA);
        assertEquals(0.20, series.get(2).getPortfolioReturn(), DELTA);
        assertEquals(0.10, series.get(2).getBenchmarkReturn(), DELTA);
        assertEquals(0.03, series.get(2).getInflationRate(), DELTA);
    }

    @Test
    void detectsWhenPortfolioTrailsBenchmarkAndInflation() {
        Portfolio portfolio = new Portfolio("p1", "achu", "Conservative Bond Portfolio");
        portfolio.addTransaction(new Transaction("t1", bonds, TransactionType.BUY, 20, 50.0, START));
        portfolioRepository.save(portfolio);

        RecordingPresenter presenter = run("achu", START, END, "TSX");

        assertNull(presenter.failMessage);
        assertEquals(0.02, presenter.successData.getPortfolioNominalReturn(), DELTA);
        assertEquals(0.05, presenter.successData.getBenchmarkNominalReturn(), DELTA);
        assertEquals(0.03, presenter.successData.getInflationRate(), DELTA);
        assertFalse(presenter.successData.isBeatingBenchmark());
        assertFalse(presenter.successData.isBeatingInflation());
        assertTrue(presenter.successData.getPortfolioRealReturn() < 0);
    }

    @Test
    void invalidDateRangeFails() {
        Portfolio portfolio = new Portfolio("p1", "achu", "My Portfolio");
        portfolio.addTransaction(new Transaction("t1", apple, TransactionType.BUY, 10, 100.0, START));
        portfolioRepository.save(portfolio);

        RecordingPresenter reversed = run("achu", END, START, "S&P 500");
        assertNull(reversed.successData);
        assertEquals("Start date must be before end date.", reversed.failMessage);

        RecordingPresenter sameDay = run("achu", START, START, "S&P 500");
        assertNull(sameDay.successData);
        assertEquals("Start date must be before end date.", sameDay.failMessage);

        RecordingPresenter missingDate = run("achu", null, END, "S&P 500");
        assertNull(missingDate.successData);
        assertEquals("Start date and end date are required.", missingDate.failMessage);
    }

    @Test
    void portfolioWithNoHistoryInRangeFails() {
        // User has no portfolio saved at all
        RecordingPresenter noPortfolio = run("nobody", START, END, "S&P 500");
        assertNull(noPortfolio.successData);
        assertEquals("Portfolio has no history in the selected date range.", noPortfolio.failMessage);

        // User only has transactions after the end date
        Portfolio futureOnly = new Portfolio("p2", "achu", "Future Portfolio");
        futureOnly.addTransaction(new Transaction(
                "t1", apple, TransactionType.BUY, 5, 130.0, LocalDate.of(2026, 8, 1)));
        portfolioRepository.save(futureOnly);

        RecordingPresenter afterRange = run("achu", START, END, "S&P 500");
        assertNull(afterRange.successData);
        assertEquals("Portfolio has no history in the selected date range.", afterRange.failMessage);
    }

    @Test
    void unavailableBenchmarkDataFails() {
        Portfolio portfolio = new Portfolio("p1", "achu", "My Portfolio");
        portfolio.addTransaction(new Transaction("t1", apple, TransactionType.BUY, 10, 100.0, START));
        portfolioRepository.save(portfolio);

        RecordingPresenter presenter = run("achu", START, END, "NASDAQ");

        assertNull(presenter.successData);
        assertEquals("Benchmark data is unavailable for NASDAQ.", presenter.failMessage);
    }

    @Test
    void unavailableCpiDataFails() {
        Portfolio portfolio = new Portfolio("p1", "achu", "My Portfolio");
        portfolio.addTransaction(new Transaction("t1", apple, TransactionType.BUY, 10, 100.0, START));
        portfolioRepository.save(portfolio);
        inflationGateway.clear();

        RecordingPresenter presenter = run("achu", START, END, "S&P 500");

        assertNull(presenter.successData);
        assertEquals("CPI data is unavailable for the selected date range.", presenter.failMessage);
    }

    @Test
    void emptyBenchmarkSelectionFails() {
        Portfolio portfolio = new Portfolio("p1", "achu", "My Portfolio");
        portfolio.addTransaction(new Transaction("t1", apple, TransactionType.BUY, 10, 100.0, START));
        portfolioRepository.save(portfolio);

        RecordingPresenter presenter = run("achu", START, END, "   ");

        assertNull(presenter.successData);
        assertEquals("Please select a benchmark.", presenter.failMessage);
    }

    @Test
    void portfolioClosedBeforeStartDateHasNoHistoryInRange() {
        Portfolio closed = new Portfolio("p1", "achu", "Closed Portfolio");
        closed.addTransaction(new Transaction(
                "t1", apple, TransactionType.BUY, 10, 80.0, LocalDate.of(2025, 1, 1)));
        closed.addTransaction(new Transaction(
                "t2", apple, TransactionType.SELL, 10, 95.0, LocalDate.of(2025, 6, 1)));
        portfolioRepository.save(closed);

        RecordingPresenter presenter = run("achu", START, END, "S&P 500");

        assertNull(presenter.successData);
        assertEquals("Portfolio has no history in the selected date range.", presenter.failMessage);
    }

    @Test
    void handlesBuysAndSellsInsideDateRange() {
        Portfolio portfolio = new Portfolio("p1", "achu", "Active Portfolio");
        // Start with 10 AAPL @ $100 ($1,000 initial value on START)
        portfolio.addTransaction(new Transaction("t1", apple, TransactionType.BUY, 10, 100.0, START));
        // Buy 10 more AAPL on MID @ $110 ($1,100 added capital -> $2,100 total base capital)
        portfolio.addTransaction(new Transaction("t2", apple, TransactionType.BUY, 10, 110.0, MID));
        // On END, 20 shares @ $120 = $2,400 -> simple total return = (2400 - 2100) / 2100 = 300 / 2100
        portfolioRepository.save(portfolio);

        RecordingPresenter presenter = run("achu", START, END, "S&P 500");

        assertNull(presenter.failMessage);
        assertEquals(300.0 / 2100.0, presenter.successData.getPortfolioNominalReturn(), DELTA);
    }
}
