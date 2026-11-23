package interface_adapter.stock_lookup;

import use_case.stock_lookup.StockLookupInputBoundary;
import use_case.stock_lookup.StockLookupInputData;

/** Translates a ticker entered in the view into a lookup request. */
public final class StockLookupController {
    private final StockLookupInputBoundary lookup;

    public StockLookupController(StockLookupInputBoundary lookup) {
        this.lookup = lookup;
    }

    public void execute(String ticker) {
        lookup.execute(new StockLookupInputData(ticker));
    }
}
