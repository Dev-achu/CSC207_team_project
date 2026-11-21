package use_case.stock_lookup;

/** Initial UC3 result; stock details will be added in a later commit. */
public class StockLookupOutputData {
    private final String ticker;

    public StockLookupOutputData(String ticker) {
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }
}
