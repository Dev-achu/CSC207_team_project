package use_case.stock_lookup;

import java.util.Locale;

/**
 * First step of UC3: check for a missing ticker and normalize user input.
 * Fetching real stock data will be implemented later.
 */
public class StockLookupInteractor implements StockLookupInputBoundary {
    private final StockLookupOutputBoundary presenter;

    public StockLookupInteractor(StockLookupOutputBoundary presenter) {
        this.presenter = presenter;
    }

    @Override
    public void execute(StockLookupInputData inputData) {
        String ticker = inputData == null ? null : inputData.getTicker();
        if (ticker == null || ticker.isBlank()) {
            presenter.prepareFailView("Please enter a stock ticker.");
            return;
        }

        String normalizedTicker = ticker.trim().toUpperCase(Locale.ROOT);
        presenter.prepareSuccessView(new StockLookupOutputData(normalizedTicker));
    }
}
