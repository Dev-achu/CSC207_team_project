package interface_adapter.stock_lookup;

import entity.PricePoint;
import entity.StockMetrics;
import use_case.stock_lookup.StockLookupOutputBoundary;
import use_case.stock_lookup.StockLookupOutputData;

import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/** Formats lookup output for the view and publishes the new state. */
public final class StockLookupPresenter implements StockLookupOutputBoundary {
    private final StockLookupViewModel viewModel;

    public StockLookupPresenter(StockLookupViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /** Formats the quote and metrics for display and prepares closing prices for the chart. */
    @Override
    public void prepareSuccessView(StockLookupOutputData output) {
        PricePoint quote = output.getQuote();
        StockMetrics metrics = output.getMetrics();
        List<PricePoint> history = output.getHistory();
        List<Double> closes = history.stream().map(PricePoint::getClose).collect(Collectors.toList());
        String firstDate = history.isEmpty() ? "" : history.get(0).getDate().toString();
        String lastDate = history.isEmpty() ? "" : history.get(history.size() - 1).getDate().toString();
        OptionalDouble ratio = metrics.getPriceToEarnings();
        OptionalDouble low = metrics.getWeek52Low();
        OptionalDouble high = metrics.getWeek52High();
        String metricsText = "P/E: " + (ratio.isPresent() ? format(ratio.getAsDouble()) : "Unavailable")
                + "     52-week low: " + (low.isPresent() ? format(low.getAsDouble()) : "Unavailable")
                + "     52-week high: " + (high.isPresent() ? format(high.getAsDouble()) : "Unavailable");

        update(new StockLookupState(output.getTicker(), quote.getSecurity().getName(),
                format(quote.getClose()), quote.getDate().toString(), quote.getClose(),
                metricsText, closes, firstDate, lastDate, output.getDataNotice()));
    }

    /** Clears the lookup display and shows the error. */
    @Override
    public void prepareFailView(String errorMessage) {
        update(StockLookupState.empty(errorMessage));
    }

    /** Stores the new state and notifies the view. */
    private void update(StockLookupState state) {
        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%,.2f", value);
    }
}
