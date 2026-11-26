package use_case.stock_lookup;

/** The ticker submitted by the user for UC3 stock lookup. */
public class StockLookupInputData {
    private final String ticker;

    /** Stores the raw ticker for the interactor to validate and normalize. */
    public StockLookupInputData(String ticker) {
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }
}
