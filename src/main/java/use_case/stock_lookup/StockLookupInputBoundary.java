package use_case.stock_lookup;

/** Entry point for the Stock Lookup use case, called by a future controller. */
public interface StockLookupInputBoundary {
    void execute(StockLookupInputData inputData);
}
