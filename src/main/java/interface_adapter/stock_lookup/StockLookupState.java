package interface_adapter.stock_lookup;

import java.util.List;

/** The display fields and plotted values for the most recent lookup. */
public final class StockLookupState {
    private final String ticker;
    private final String name;
    private final String quote;
    private final String tradingDay;
    private final double price;
    private final String metrics;
    private final List<Double> closes;
    private final String firstDate;
    private final String lastDate;
    private final String status;

    StockLookupState(String ticker, String name, String quote, String tradingDay, double price,
                     String metrics, List<Double> closes, String firstDate, String lastDate, String status) {
        this.ticker = ticker;
        this.name = name;
        this.quote = quote;
        this.tradingDay = tradingDay;
        this.price = price;
        this.metrics = metrics;
        this.closes = List.copyOf(closes);
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.status = status;
    }

    static StockLookupState empty(String status) {
        return new StockLookupState("", "", "", "", 0, "", List.of(), "", "", status);
    }

    public boolean hasResult() {
        return !ticker.isEmpty();
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public String getQuote() {
        return quote;
    }

    public String getTradingDay() {
        return tradingDay;
    }

    public double getPrice() {
        return price;
    }

    public String getMetrics() {
        return metrics;
    }

    public List<Double> getCloses() {
        return closes;
    }

    public String getFirstDate() {
        return firstDate;
    }

    public String getLastDate() {
        return lastDate;
    }

    public String getStatus() {
        return status;
    }
}
