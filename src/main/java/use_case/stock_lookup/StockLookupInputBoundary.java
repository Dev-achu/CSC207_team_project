package use_case.stock_lookup;

/** Entry point for the Stock Lookup use case. */
public interface StockLookupInputBoundary {
    /** Runs a lookup for the submitted ticker input. */
    void execute(StockLookupInputData inputData);
}
