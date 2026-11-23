package use_case.stock_lookup;

import java.util.Locale;

/** Validates the ticker and retrieves the information needed to evaluate it. */
public class StockLookupInteractor implements StockLookupInputBoundary {
    private final StockLookupDataAccessInterface stockData;
    private final StockLookupOutputBoundary presenter;

    public StockLookupInteractor(StockLookupDataAccessInterface stockData, StockLookupOutputBoundary presenter) {
        this.stockData = stockData;
        this.presenter = presenter;
    }

    @Override
    public void execute(StockLookupInputData inputData) {
        String ticker = inputData == null ? null : inputData.getTicker();
        if (ticker == null || ticker.isBlank()) {
            presenter.prepareFailView("Please enter a stock ticker.");
            return;
        }

        String normalizedTicker = ticker.strip().toUpperCase(Locale.ROOT);
        if (!normalizedTicker.matches("[A-Z0-9][A-Z0-9.:-]{0,23}")) {
            presenter.prepareFailView("Enter a valid ticker, such as AAPL or BRK.B.");
            return;
        }

        try {
            StockLookupDataAccessInterface.Result result = stockData.lookup(normalizedTicker);
            String reason = result.getCacheReason();
            String notice = reason.isEmpty() ? "" : "Showing cached data. " + reason;
            presenter.prepareSuccessView(new StockLookupOutputData(result.getSnapshot(), notice));
        } catch (StockDataException failure) {
            presenter.prepareFailView(failure.getMessage());
        }
    }
}
