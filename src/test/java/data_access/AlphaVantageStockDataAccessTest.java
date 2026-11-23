package data_access;

import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals("Apple Inc.", dataAccess.findSecurity("AAPL").getName());
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
