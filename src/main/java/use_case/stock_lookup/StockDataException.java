package use_case.stock_lookup;

/** A market data failure with a message suitable for display. */
public class StockDataException extends Exception {
    public StockDataException(String message) {
        super(message);
    }
}
