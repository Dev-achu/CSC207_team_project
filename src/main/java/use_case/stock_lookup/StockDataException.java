package use_case.stock_lookup;

/** A market data failure with a message suitable for display. */
public class StockDataException extends Exception {
    /** Carries a message that can be shown to the user. */
    public StockDataException(String message) {
        super(message);
    }
}
