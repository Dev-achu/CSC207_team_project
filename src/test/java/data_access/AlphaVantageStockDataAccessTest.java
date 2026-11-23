package data_access;

import entity.Portfolio;
import entity.Security;
import entity.StockSnapshot;
import entity.Transaction;
import entity.TransactionType;
import interface_adapter.dashboard.AddTransactionController;
import interface_adapter.dashboard.DashboardPresenter;
import interface_adapter.dashboard.DashboardViewModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import use_case.dashboard.AddTransactionInteractor;
import use_case.stock_lookup.StockDataException;
import use_case.stock_lookup.StockLookupDataAccessInterface;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AlphaVantageStockDataAccessTest {
    private static final String QUOTE = "{\"Global Quote\":{\"05. price\":\"191.25\","
            + "\"07. latest trading day\":\"2026-01-06\"}}";
    private static final String DAILY = "{\"Time Series (Daily)\":{\"2026-01-06\":{\"4. close\":\"191.25\"},"
            + "\"2026-01-05\":{\"4. close\":\"190.00\"}}}";
    private static final String OVERVIEW = "{\"Name\":\"Apple Inc.\",\"PERatio\":\"25.4\","
            + "\"52WeekLow\":\"None\",\"52WeekHigh\":\"220.00\"}";

    @Test
    void parsesQuoteAndSortedHistoryAndCachesRequestsForDashboard() throws Exception {
        List<URI> requests = new ArrayList<>();
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri -> {
            requests.add(uri);
            return response(uri);
        });

        StockLookupDataAccessInterface.Result result = dataAccess.lookup("AAPL");
        assertEquals("Apple Inc.", result.getSnapshot().getQuote().getSecurity().getName());
        assertEquals(191.25, result.getSnapshot().getQuote().getClose());
        assertEquals(LocalDate.of(2026, 1, 5), result.getSnapshot().getHistory().get(0).getDate());
        assertEquals(25.4, result.getSnapshot().getMetrics().getPriceToEarnings().orElseThrow());
        assertFalse(result.getSnapshot().getMetrics().getWeek52Low().isPresent());
        assertEquals(220.0, result.getSnapshot().getMetrics().getWeek52High().orElseThrow());

        dataAccess.lookup("AAPL");
        Security security = dataAccess.findSecurity("AAPL");
        assertNotNull(security);
        assertEquals("Apple Inc.", security.getName());
        assertEquals(191.25, dataAccess.getCurrentPrice("AAPL"));
        assertEquals(3, requests.size());
        assertTrue(requests.get(0).toString().contains("function=GLOBAL_QUOTE&symbol=AAPL"));
    }

    @Test
    void emptyOverviewStillReturnsQuoteAndHistory() throws Exception {
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri ->
                uri.getQuery().contains("function=OVERVIEW") ? "{}" : response(uri));

        StockLookupDataAccessInterface.Result result = dataAccess.lookup("AAPL");

        assertEquals(2, result.getSnapshot().getHistory().size());
        assertFalse(result.getSnapshot().getMetrics().getPriceToEarnings().isPresent());
    }

    @Test
    void emptyQuoteReportsUnknownTicker() {
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri -> "{\"Global Quote\":{}}");

        StockDataException failure = assertThrows(StockDataException.class, () -> dataAccess.lookup("UNKNOWN"));

        assertEquals("Ticker not found: UNKNOWN.", failure.getMessage());
    }

    @Test
    void rateLimitInformationBecomesUserFacingError() {
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri ->
                "{\"Information\":\"Our standard API rate limit is 25 requests per day.\"}");

        StockDataException failure = assertThrows(StockDataException.class, () -> dataAccess.lookup("AAPL"));

        assertTrue(failure.getMessage().contains("request limit"));
    }

    @Test
    void networkFailureUsesLastSuccessfulSnapshotAfterExpiry() throws Exception {
        MutableClock clock = new MutableClock();
        AlphaVantageStockDataAccess dataAccess = new AlphaVantageStockDataAccess("test-key", clock, uri -> {
            if (clock.instant().isAfter(Instant.EPOCH)) {
                throw new IOException("offline");
            }
            return response(uri);
        });

        StockLookupDataAccessInterface.Result original = dataAccess.lookup("AAPL");
        clock.now = clock.now.plus(Duration.ofHours(2));
        StockLookupDataAccessInterface.Result cached = dataAccess.lookup("AAPL");

        assertEquals(original.getSnapshot(), cached.getSnapshot());
        assertTrue(cached.getCacheReason().contains("Could not reach Alpha Vantage"));
        assertThrows(StockDataException.class, () -> dataAccess.lookup("MSFT"));
    }

    @Test
    void missingApiKeyNeverSendsARequest() {
        AlphaVantageStockDataAccess dataAccess = new AlphaVantageStockDataAccess("", Clock.systemUTC(),
                uri -> fail("A lookup without an API key should not send a request."));

        StockDataException failure = assertThrows(StockDataException.class, () -> dataAccess.lookup("AAPL"));

        assertTrue(failure.getMessage().contains("ALPHAVANTAGE_API_KEY"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[", "[]"})
    void malformedOrNonObjectResponsesAreRejected(String body) {
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri -> body);

        assertInvalidResponse(dataAccess);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "0"})
    void invalidQuotePricesAreRejected(String price) {
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri -> QUOTE.replace("191.25", price));

        assertInvalidResponse(dataAccess);
    }

    @Test
    void missingHistoryReportsUnavailable() {
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri ->
                uri.getQuery().contains("TIME_SERIES_DAILY") ? "{}" : response(uri));

        StockDataException failure = assertThrows(StockDataException.class, () -> dataAccess.lookup("AAPL"));

        assertEquals("Price history is unavailable for AAPL.", failure.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"Time Series (Daily)\":{\"yesterday\":{\"4. close\":190}}}",
        "{\"Time Series (Daily)\":{\"2026-01-05\":{}}}"
    })
    void malformedHistoryRowsFailTheLookup(String body) {
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri ->
                uri.getQuery().contains("TIME_SERIES_DAILY") ? body : response(uri));

        assertInvalidResponse(dataAccess);
    }

    @Test
    void accessDenialDoesNotExposeProviderMessage() {
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri ->
                "{\"Information\":\"Invalid API key test-key\"}");

        StockDataException failure = assertThrows(StockDataException.class, () -> dataAccess.lookup("AAPL"));

        assertEquals("Alpha Vantage denied the request. Check your API key and plan.", failure.getMessage());
        assertFalse(failure.getMessage().contains("test-key"));
    }

    @Test
    void cacheIsIsolatedByTickerAndRefreshesAtExpiration() throws Exception {
        MutableClock clock = new MutableClock();
        List<URI> requests = new ArrayList<>();
        AlphaVantageStockDataAccess dataAccess = new AlphaVantageStockDataAccess(" test&key ", clock, uri -> {
            requests.add(uri);
            return clock.instant().equals(Instant.EPOCH) ? response(uri) : response(uri).replace("191.25", "200");
        });
        StockSnapshot first = dataAccess.lookup("AAPL").getSnapshot();
        assertEquals("MSFT", dataAccess.lookup("MSFT").getSnapshot().getQuote().getSecurity().getTicker());

        clock.now = clock.now.plus(Duration.ofMinutes(59));
        assertSame(first, dataAccess.lookup("AAPL").getSnapshot());
        assertEquals(6, requests.size());
        assertTrue(requests.get(0).getRawQuery().endsWith("apikey=test%26key"));

        clock.now = clock.now.plus(Duration.ofMinutes(1));
        StockLookupDataAccessInterface.Result refreshed = dataAccess.lookup("AAPL");
        assertEquals(200.0, refreshed.getSnapshot().getQuote().getClose());
        assertTrue(refreshed.getCacheReason().isEmpty());
        assertEquals(9, requests.size());
    }

    @Test
    void buyAfterStockLookupRecordsTradeAndReusesMarketQuote() throws Exception {
        List<URI> requests = new ArrayList<>();
        AlphaVantageStockDataAccess dataAccess = dataAccess(uri -> {
            requests.add(uri);
            return response(uri);
        });
        InMemoryPortfolioRepository portfolios = new InMemoryPortfolioRepository();
        DashboardViewModel dashboard = new DashboardViewModel();
        AddTransactionController controller = new AddTransactionController(
                new AddTransactionInteractor(portfolios, dataAccess, new DashboardPresenter(dashboard)));
        StockSnapshot lookup = dataAccess.lookup("AAPL").getSnapshot();
        LocalDate tradeDate = LocalDate.of(2026, 1, 4);

        controller.execute("diego", lookup.getQuote().getSecurity().getTicker(), "BUY", 2, 175, tradeDate);

        Portfolio portfolio = portfolios.findByOwner("diego");
        assertNotNull(portfolio);
        assertEquals(1, portfolio.getTransactions().size());
        Transaction trade = portfolio.getTransactions().get(0);
        assertEquals(TransactionType.BUY, trade.getType());
        assertEquals(tradeDate, trade.getDate());
        assertEquals(175.0, trade.getPrice());
        assertEquals(2.0, portfolio.getQuantityOwned("AAPL"));
        assertEquals("$382.50", dashboard.getState().getTotalValue());
        assertTrue(dashboard.getState().getError().isEmpty());
        assertEquals(3, requests.size());
    }

    private void assertInvalidResponse(AlphaVantageStockDataAccess dataAccess) {
        StockDataException failure = assertThrows(StockDataException.class, () -> dataAccess.lookup("AAPL"));
        assertEquals("Alpha Vantage returned an invalid response.", failure.getMessage());
    }

    private AlphaVantageStockDataAccess dataAccess(AlphaVantageStockDataAccess.ResponseSource source) {
        return new AlphaVantageStockDataAccess("test-key", Clock.systemUTC(), source);
    }

    private static String response(URI uri) {
        String function = uri.getQuery().split("&", 2)[0];
        switch (function) {
            case "function=GLOBAL_QUOTE":
                return QUOTE;
            case "function=TIME_SERIES_DAILY":
                return DAILY;
            case "function=OVERVIEW":
                return OVERVIEW;
            default:
                throw new AssertionError("Unexpected function: " + function);
        }
    }

    private static class MutableClock extends Clock {
        private Instant now = Instant.EPOCH;

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(now, zone);
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
