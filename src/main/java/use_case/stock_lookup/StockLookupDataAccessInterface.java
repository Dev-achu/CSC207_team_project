package use_case.stock_lookup;

import entity.StockSnapshot;

/** Market data port implemented by the external provider adapter. */
public interface StockLookupDataAccessInterface {
    Result lookup(String ticker) throws StockDataException;

    /** A snapshot and an optional explanation of why cached data was used. */
    final class Result {
        private final StockSnapshot snapshot;
        private final String cacheReason;

        public Result(StockSnapshot snapshot, String cacheReason) {
            this.snapshot = snapshot;
            this.cacheReason = cacheReason;
        }

        public StockSnapshot getSnapshot() {
            return snapshot;
        }

        public String getCacheReason() {
            return cacheReason;
        }
    }
}
