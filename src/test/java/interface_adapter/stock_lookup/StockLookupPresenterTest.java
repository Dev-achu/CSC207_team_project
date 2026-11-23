package interface_adapter.stock_lookup;

import entity.AssetClass;
import entity.PricePoint;
import entity.Security;
import entity.StockMetrics;
import entity.StockSnapshot;
import org.junit.jupiter.api.Test;
import use_case.stock_lookup.StockLookupOutputData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockLookupPresenterTest {
    private final StockLookupViewModel viewModel = new StockLookupViewModel();
    private final StockLookupPresenter presenter = new StockLookupPresenter(viewModel);
    private final Security security = new Security("AAPL", "Apple Inc.", AssetClass.STOCK);
    private final PricePoint first = new PricePoint(security, LocalDate.of(2026, 1, 5), 1200);
    private final PricePoint quote = new PricePoint(security, LocalDate.of(2026, 1, 6), 1234.5);

    @Test
    void presenterPublishesFormattedQuoteMetricsAndChartData() {
        List<StockLookupState> events = new ArrayList<>();
        viewModel.addPropertyChangeListener(event -> {
            assertEquals("state", event.getPropertyName());
            events.add((StockLookupState) event.getNewValue());
        });
        StockSnapshot snapshot = new StockSnapshot(quote, List.of(first, quote),
                new StockMetrics(26.4, 990.0, 1300.0));

        presenter.prepareSuccessView(new StockLookupOutputData(snapshot, "Showing cached data."));

        StockLookupState state = viewModel.getState();
        assertEquals(1, events.size());
        assertSame(state, events.get(0));
        assertTrue(state.hasResult());
        assertEquals("AAPL", state.getTicker());
        assertEquals("Apple Inc.", state.getName());
        assertEquals(1234.5, state.getPrice());
        assertEquals("1,234.50", state.getQuote());
        assertEquals("2026-01-06", state.getTradingDay());
        assertEquals("P/E: 26.40     52-week low: 990.00     52-week high: 1,300.00", state.getMetrics());
        assertEquals(List.of(1200.0, 1234.5), state.getCloses());
        assertEquals("2026-01-05", state.getFirstDate());
        assertEquals("2026-01-06", state.getLastDate());
        assertEquals("Showing cached data.", state.getStatus());
    }

}
