package use_case.stock_lookup;

import entity.PricePoint;
import entity.StockMetrics;
import entity.StockSnapshot;

import java.util.List;

/** The quote, history, fundamentals, and any data notice for a lookup. */
public class StockLookupOutputData {
    private final StockSnapshot snapshot;
    private final String dataNotice;

    public StockLookupOutputData(StockSnapshot snapshot, String dataNotice) {
        this.snapshot = snapshot;
        this.dataNotice = dataNotice;
    }

    public String getTicker() {
        return getQuote().getSecurity().getTicker();
    }

    public PricePoint getQuote() {
        return snapshot.getQuote();
    }

    public List<PricePoint> getHistory() {
        return snapshot.getHistory();
    }

    public StockMetrics getMetrics() {
        return snapshot.getMetrics();
    }

    public String getDataNotice() {
        return dataNotice;
    }
}
