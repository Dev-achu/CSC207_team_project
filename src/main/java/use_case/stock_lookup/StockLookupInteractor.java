package use_case.stock_lookup;

import java.util.Locale;
import java.util.StringJoiner;

/** Coordinates stock lookup between the market data provider and the presenter. */
public class StockLookupInteractor implements StockLookupInputBoundary {
    private final StockLookupDataAccessInterface stockData;
    private final StockLookupOutputBoundary presenter;

    public StockLookupInteractor(StockLookupDataAccessInterface stockData, StockLookupOutputBoundary presenter) {
        this.stockData = stockData;
        this.presenter = presenter;
    }

    /** Validates the ticker and sends the lookup result and any notices to the presenter. */
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
            // A stale snapshot can carry both a cache notice and a fundamentals warning.
            StringJoiner notice = new StringJoiner(" ");
            if (!reason.isEmpty()) {
                notice.add("Showing cached data. " + reason);
            }
            if (!result.getFundamentalsWarning().isEmpty()) {
                notice.add(result.getFundamentalsWarning());
            }
            presenter.prepareSuccessView(new StockLookupOutputData(result.getSnapshot(), notice.toString()));
        } catch (StockDataException failure) {
            presenter.prepareFailView(failure.getMessage());
        }
    }
}
