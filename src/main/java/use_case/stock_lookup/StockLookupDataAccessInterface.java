package use_case.stock_lookup;

import entity.StockSnapshot;

/** Market data port implemented by the external provider adapter. */
public interface StockLookupDataAccessInterface {
    Result lookup(String ticker) throws StockDataException;

    /** A snapshot with optional cache and fundamentals messages. */
    final class Result {
        private final StockSnapshot snapshot;
        private final String cacheReason;
        private final String fundamentalsWarning;

        public Result(StockSnapshot snapshot, String cacheReason) {
            this(snapshot, cacheReason, "");
        }

        public Result(StockSnapshot snapshot, String cacheReason, String fundamentalsWarning) {
            this.snapshot = snapshot;
            this.cacheReason = cacheReason;
            this.fundamentalsWarning = fundamentalsWarning;
        }

        public StockSnapshot getSnapshot() {
            return snapshot;
        }

        public String getCacheReason() {
            return cacheReason;
        }

        public String getFundamentalsWarning() {
            return fundamentalsWarning;
        }
    }
}
