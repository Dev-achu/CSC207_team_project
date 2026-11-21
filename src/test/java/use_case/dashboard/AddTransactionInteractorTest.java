package use_case.dashboard;

import data_access.InMemoryPortfolioRepository;
import entity.AssetClass;
import entity.Portfolio;
import entity.Security;
import entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AddTransactionInteractorTest {
    private static final double DELTA = 1e-9;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 20);

    private InMemoryPortfolioRepository repository;
    private StubStockData stockData;

    /** A fake market data source so tests never touch a real API. */
    private static class StubStockData implements DashboardStockDataAccessInterface {
        private final Map<String, Security> securities = new HashMap<>();
        private final Map<String, Double> prices = new HashMap<>();
        private final Set<String> failingTickers = new HashSet<>();

        void add(Security security, double price) {
            securities.put(security.getTicker(), security);
            prices.put(security.getTicker(), price);
        }

        @Override
        public Security findSecurity(String ticker) {
            return securities.get(ticker);
        }

        @Override
        public double getCurrentPrice(String ticker) throws PriceUnavailableException {
            if (failingTickers.contains(ticker)) {
                throw new PriceUnavailableException("API down");
            }
            return prices.get(ticker);
        }
    }

    /** Remembers what the interactor sent, so tests can check it. */
    private static class RecordingPresenter implements AddTransactionOutputBoundary {
        AddTransactionOutputData successData;
        String failMessage;

        @Override
        public void prepareSuccessView(AddTransactionOutputData outputData) {
            successData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            failMessage = errorMessage;
        }
    }

    @BeforeEach
    void setUp() {
        repository = new InMemoryPortfolioRepository();
        stockData = new StubStockData();
        stockData.add(new Security("AAPL", "Apple Inc.", AssetClass.STOCK), 150);
        stockData.add(new Security("BND", "Vanguard Total Bond Market ETF", AssetClass.BOND), 50);
    }

    private RecordingPresenter run(String ticker, TransactionType type, double quantity, double price) {
        RecordingPresenter presenter = new RecordingPresenter();
        AddTransactionInteractor interactor = new AddTransactionInteractor(repository, stockData, presenter);
        interactor.execute(new AddTransactionInputData("ryan", ticker, type, quantity, price, DATE));
        return presenter;
    }

    @Test
    void firstBuyCreatesPortfolioAndReportsValue() {
        RecordingPresenter presenter = run("AAPL", TransactionType.BUY, 10, 100);

        assertNull(presenter.failMessage);
        assertEquals(1500, presenter.successData.getTotalValue(), DELTA);
        assertEquals(1.0, presenter.successData.getAllocation().get("STOCK"), DELTA);
        assertEquals(1, presenter.successData.getHoldings().size());
        assertNotNull(repository.findByOwner("ryan"));
    }

    @Test
    void allocationIsSplitByAssetClass() {
        run("AAPL", TransactionType.BUY, 10, 100);
        RecordingPresenter presenter = run("BND", TransactionType.BUY, 20, 50);

        assertEquals(2500, presenter.successData.getTotalValue(), DELTA);
        assertEquals(0.6, presenter.successData.getAllocation().get("STOCK"), DELTA);
        assertEquals(0.4, presenter.successData.getAllocation().get("BOND"), DELTA);
    }

    @Test
    void lowercaseTickerIsAccepted() {
        RecordingPresenter presenter = run("aapl", TransactionType.BUY, 1, 100);

        assertNull(presenter.failMessage);
        assertEquals("AAPL", presenter.successData.getHoldings().get(0).getTicker());
    }

    @Test
    void unknownTickerFails() {
        RecordingPresenter presenter = run("ZZZZ", TransactionType.BUY, 1, 10);

        assertNull(presenter.successData);
        assertEquals("Unknown ticker: ZZZZ", presenter.failMessage);
        assertNull(repository.findByOwner("ryan"));
    }

    @Test
    void sellingMoreThanOwnedFailsAndChangesNothing() {
        run("AAPL", TransactionType.BUY, 5, 100);
        RecordingPresenter presenter = run("AAPL", TransactionType.SELL, 6, 100);

        assertNull(presenter.successData);
        assertTrue(presenter.failMessage.startsWith("Cannot sell"));
        Portfolio saved = repository.findByOwner("ryan");
        assertEquals(5, saved.getQuantityOwned("AAPL"), DELTA);
    }

    @Test
    void negativeQuantityFails() {
        RecordingPresenter presenter = run("AAPL", TransactionType.BUY, -1, 100);

        assertEquals("Quantity must be greater than 0.", presenter.failMessage);
    }

    @Test
    void negativePriceFails() {
        RecordingPresenter presenter = run("AAPL", TransactionType.BUY, 1, -100);

        assertEquals("Price must be greater than 0.", presenter.failMessage);
    }

    @Test
    void missingDateFails() {
        RecordingPresenter presenter = new RecordingPresenter();
        new AddTransactionInteractor(repository, stockData, presenter)
                .execute(new AddTransactionInputData("ryan", "AAPL", TransactionType.BUY, 1, 100, null));

        assertEquals("Please enter a trade date.", presenter.failMessage);
    }

    @Test
    void priceFetchFailureFallsBackToLastKnownPrice() {
        stockData.failingTickers.add("AAPL");
        RecordingPresenter presenter = run("AAPL", TransactionType.BUY, 10, 120);

        if (presenter.successData == null) {
            fail("Expected success, got: " + presenter.failMessage);
        }
        assertEquals(1200, presenter.successData.getTotalValue(), DELTA);
        assertEquals(1, presenter.successData.getStalePriceTickers().size());
        assertEquals("AAPL", presenter.successData.getStalePriceTickers().get(0));
    }
}
