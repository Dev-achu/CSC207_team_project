package use_case.stock_lookup;

import entity.StockSnapshot;

/** Market data port implemented by the external provider adapter. */
public interface StockLookupDataAccessInterface {
    /** Retrieves the quote and price history, with fundamentals when available. */
    Result lookup(String ticker) throws StockDataException;

    /** Bundles the market data with any messages the lookup should display. */
    final class Result {
        private final StockSnapshot snapshot;
        private final String cacheReason;
        private final String fundamentalsWarning;

        public Result(StockSnapshot snapshot, String cacheReason) {
            this(snapshot, cacheReason, "");
        }

        /** Keeps the cache reason and fundamentals warning available to the use case. */
        public Result(StockSnapshot snapshot, String cacheReason, String fundamentalsWarning) {
            this.snapshot = snapshot;
            this.cacheReason = cacheReason;
            this.fundamentalsWarning = fundamentalsWarning;
        }

        public StockSnapshot getSnapshot() {
            return snapshot;
        }

        /** Explains why stale data was reused, or returns an empty string. */
        public String getCacheReason() {
            return cacheReason;
        }

        /** Returns the warning about unavailable fundamentals, or an empty string. */
        public String getFundamentalsWarning() {
            return fundamentalsWarning;
        }
    }
}
