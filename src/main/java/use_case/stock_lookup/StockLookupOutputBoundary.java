package use_case.stock_lookup;

/** Presents the result of executing the Stock Lookup use case. */
public interface StockLookupOutputBoundary {
    void prepareSuccessView(StockLookupOutputData outputData);

    void prepareFailView(String errorMessage);
}
