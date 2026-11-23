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

    @Override
    public void prepareSuccessView(StockLookupOutputData output) {
        PricePoint quote = output.getQuote();
        StockMetrics metrics = output.getMetrics();
        List<PricePoint> history = output.getHistory();
        List<Double> closes = history.stream().map(PricePoint::getClose).collect(Collectors.toList());
        String firstDate = history.isEmpty() ? "" : history.get(0).getDate().toString();
        String lastDate = history.isEmpty() ? "" : history.get(history.size() - 1).getDate().toString();
        String metricsText = "P/E: " + display(metrics.getPriceToEarnings())
                + "     52-week low: " + display(metrics.getWeek52Low())
                + "     52-week high: " + display(metrics.getWeek52High());

        update(new StockLookupState(output.getTicker(), quote.getSecurity().getName(),
                format(quote.getClose()), quote.getDate().toString(), quote.getClose(),
                metricsText, closes, firstDate, lastDate, output.getDataNotice()));
    }

    @Override
    public void prepareFailView(String errorMessage) {
        update(StockLookupState.empty(errorMessage));
    }

    private void update(StockLookupState state) {
        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    private String display(OptionalDouble metric) {
        return metric.isPresent() ? format(metric.getAsDouble()) : "Unavailable";
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%,.2f", value);
    }
}
