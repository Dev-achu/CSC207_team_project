package entity;

import java.util.List;
import java.util.Objects;

/** A security's latest quote, daily closing prices, and fundamentals. */
public final class StockSnapshot {
    private final PricePoint quote;
    private final List<PricePoint> history;
    private final StockMetrics metrics;

    public StockSnapshot(PricePoint quote, List<PricePoint> history, StockMetrics metrics) {
        this.quote = Objects.requireNonNull(quote);
        this.history = List.copyOf(history);
        this.metrics = Objects.requireNonNull(metrics);
    }

    public PricePoint getQuote() {
        return quote;
    }

    public List<PricePoint> getHistory() {
        return history;
    }

    public StockMetrics getMetrics() {
        return metrics;
    }
}
