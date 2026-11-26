package use_case.stock_lookup;

/** Presents the result of executing the Stock Lookup use case. */
public interface StockLookupOutputBoundary {
    /** Receives the market data and any notices to display. */
    void prepareSuccessView(StockLookupOutputData outputData);

    /** Receives the error to display when a lookup fails. */
    void prepareFailView(String errorMessage);
}
