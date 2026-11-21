package use_case.stock_lookup;

/** The presenter will implement this when the UC3 UI is added. */
public interface StockLookupOutputBoundary {
    void prepareSuccessView(StockLookupOutputData outputData);

    void prepareFailView(String errorMessage);
}
