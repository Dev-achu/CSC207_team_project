package data_access;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import entity.AssetClass;
import entity.PricePoint;
import entity.Security;
import entity.StockMetrics;
import entity.StockSnapshot;
import use_case.dashboard.DashboardStockDataAccessInterface;
import use_case.dashboard.PriceUnavailableException;
import use_case.stock_lookup.StockDataException;
import use_case.stock_lookup.StockLookupDataAccessInterface;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Alpha Vantage market data shared by lookup and the portfolio dashboard. */
public final class AlphaVantageStockDataAccess
        implements StockLookupDataAccessInterface, DashboardStockDataAccessInterface {
    private static final Duration CACHE_DURATION = Duration.ofHours(1);
    private static final String RATE_LIMIT = "Alpha Vantage's request limit was reached. Try again later.";
    private static final String INVALID_DATA = "Alpha Vantage returned an invalid response.";
    private static final String ACCESS_DENIED = "Alpha Vantage denied the request. Check your API key and plan.";

    private final String apiKey;
    private final Clock clock;
    private final ResponseSource source;
    private final Map<String, Cached<PricePoint>> quotes = new HashMap<>();
    private final Map<String, Cached<StockSnapshot>> lookups = new HashMap<>();

    public AlphaVantageStockDataAccess(String apiKey) {
        this(apiKey, Clock.systemUTC(), httpSource());
    }

    AlphaVantageStockDataAccess(String apiKey, Clock clock, ResponseSource source) {
        this.apiKey = apiKey == null ? "" : apiKey.strip();
        this.clock = clock;
        this.source = source;
    }

    @Override
    public synchronized Result lookup(String ticker) throws StockDataException {
        Cached<StockSnapshot> previous = lookups.get(ticker);
        if (isFresh(previous)) {
            return new Result(previous.value, "");
        }

        try {
            PricePoint latest = fetchQuote(ticker);
            JsonObject daily = child(request("TIME_SERIES_DAILY", ticker), "Time Series (Daily)");
            if (daily.isEmpty()) {
                throw new StockDataException("Price history is unavailable for " + ticker + ".");
            }

            JsonObject overview = request("OVERVIEW", ticker);
            String name = field(overview, "Name");
            Security security = new Security(ticker, name.isBlank() ? ticker : name, AssetClass.STOCK);
            List<PricePoint> history = parseHistory(daily, security);
            StockMetrics metrics = new StockMetrics(number(overview, "PERatio"),
                    number(overview, "52WeekLow"), number(overview, "52WeekHigh"));
            PricePoint quote = new PricePoint(security, latest.getDate(), latest.getClose());
            StockSnapshot snapshot = new StockSnapshot(quote, history, metrics);
            lookups.put(ticker, new Cached<>(snapshot, clock.instant()));
            return new Result(snapshot, "");
        } catch (StockDataException failure) {
            if (previous == null) {
                throw failure;
            }
            return new Result(previous.value, failure.getMessage());
        }
    }

    @Override
    public synchronized Security findSecurity(String ticker) {
        Cached<StockSnapshot> known = lookups.get(ticker);
        if (known != null) {
            return known.value.getQuote().getSecurity();
        }

        try {
            return fetchQuote(ticker).getSecurity();
        } catch (StockDataException failure) {
            Cached<PricePoint> previous = quotes.get(ticker);
            return previous == null ? null : previous.value.getSecurity();
        }
    }

    @Override
    public synchronized double getCurrentPrice(String ticker) throws PriceUnavailableException {
        try {
            return fetchQuote(ticker).getClose();
        } catch (StockDataException failure) {
            throw new PriceUnavailableException(failure.getMessage());
        }
    }

    private PricePoint fetchQuote(String ticker) throws StockDataException {
        Cached<PricePoint> previous = quotes.get(ticker);
        if (isFresh(previous)) {
            return previous.value;
        }

        JsonObject response = request("GLOBAL_QUOTE", ticker);
        JsonObject data = child(response, "Global Quote");
        if (data.isEmpty()) {
            throw new StockDataException("Ticker not found: " + ticker + ".");
        }

        try {
            Security security = new Security(ticker, ticker, AssetClass.STOCK);
            LocalDate date = LocalDate.parse(field(data, "07. latest trading day"));
            PricePoint quote = new PricePoint(security, date, price(data, "05. price"));
            quotes.put(ticker, new Cached<>(quote, clock.instant()));
            return quote;
        } catch (DateTimeParseException | IllegalArgumentException failure) {
            throw new StockDataException(INVALID_DATA);
        }
    }

    private List<PricePoint> parseHistory(JsonObject daily, Security security) throws StockDataException {
        List<PricePoint> history = new ArrayList<>();
        try {
            for (Map.Entry<String, JsonElement> entry : daily.entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey());
                JsonObject row = child(daily, entry.getKey());
                history.add(new PricePoint(security, date, price(row, "4. close")));
            }
        } catch (DateTimeParseException | IllegalArgumentException failure) {
            throw new StockDataException(INVALID_DATA);
        }
        history.sort(Comparator.comparing(PricePoint::getDate));
        return history;
    }

    private JsonObject request(String function, String ticker) throws StockDataException {
        if (apiKey.isBlank()) {
            throw new StockDataException("Set ALPHAVANTAGE_API_KEY to look up stocks.");
        }

        URI uri = URI.create("https://www.alphavantage.co/query?function=" + function
                + "&symbol=" + encode(ticker) + "&apikey=" + encode(apiKey));
        try {
            JsonElement payload = JsonParser.parseString(source.fetch(uri));
            if (!payload.isJsonObject()) {
                throw new StockDataException(INVALID_DATA);
            }
            JsonObject data = payload.getAsJsonObject();
            checkProviderMessage(data, function, ticker);
            return data;
        } catch (IOException failure) {
            throw new StockDataException("Could not reach Alpha Vantage. Check your connection and try again.");
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new StockDataException("The stock data request was interrupted.");
        } catch (JsonParseException failure) {
            throw new StockDataException(INVALID_DATA);
        }
    }

    private void checkProviderMessage(JsonObject data, String function, String ticker)
            throws StockDataException {
        String information = field(data, "Information").toLowerCase(Locale.ROOT);
        if (data.has("Note") || information.contains("rate limit")
                || information.contains("requests per") || information.contains("call frequency")) {
            throw new StockDataException(RATE_LIMIT);
        }
        if (data.has("Information")) {
            throw new StockDataException(ACCESS_DENIED);
        }
        if (data.has("Error Message")) {
            if ("GLOBAL_QUOTE".equals(function)) {
                throw new StockDataException("Ticker not found: " + ticker + ".");
            }
            throw new StockDataException("Alpha Vantage rejected " + function + " for " + ticker + ".");
        }
    }

    private boolean isFresh(Cached<?> cached) {
        return cached != null && clock.instant().isBefore(cached.fetchedAt.plus(CACHE_DURATION));
    }

    private static JsonObject child(JsonObject parent, String key) {
        JsonElement value = parent.get(key);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    private static String field(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString().strip() : "";
    }

    private static Double number(JsonObject object, String key) {
        try {
            double parsed = Double.parseDouble(field(object, key));
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException failure) {
            return null;
        }
    }

    private static double price(JsonObject object, String key) {
        Double value = number(object, key);
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid price.");
        }
        return value;
    }

    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private static ResponseSource httpSource() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        return uri -> {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(12)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                throw new StockDataException(RATE_LIMIT);
            }
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new StockDataException(ACCESS_DENIED);
            }
            if (response.statusCode() != 200) {
                throw new StockDataException("Alpha Vantage is unavailable (HTTP " + response.statusCode() + ").");
            }
            return response.body();
        };
    }

    @FunctionalInterface
    interface ResponseSource {
        String fetch(URI uri) throws IOException, InterruptedException, StockDataException;
    }

    private static final class Cached<T> {
        private final T value;
        private final Instant fetchedAt;

        Cached(T value, Instant fetchedAt) {
            this.value = value;
            this.fetchedAt = fetchedAt;
        }
    }
}
