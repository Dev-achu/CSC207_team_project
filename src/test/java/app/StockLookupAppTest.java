package app;

import data_access.InMemoryPortfolioRepository;
import entity.AssetClass;
import entity.PricePoint;
import entity.Security;
import entity.StockMetrics;
import entity.StockSnapshot;
import org.junit.jupiter.api.Test;
import use_case.dashboard.DashboardStockDataAccessInterface;
import use_case.stock_lookup.StockLookupDataAccessInterface;
import view.StockLookupView;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockLookupAppTest {
    @Test
    void screenDisplaysLookupResultFromTheWiredUseCase() throws Exception {
        LocalDate date = LocalDate.of(2026, 1, 5);
        Security apple = new Security("AAPL", "Apple Inc.", AssetClass.STOCK);
        PricePoint quote = new PricePoint(apple, date, 190);
        StockSnapshot snapshot = new StockSnapshot(quote, List.of(quote), new StockMetrics(20.0, null, 220.0));
        AtomicReference<String> submittedTicker = new AtomicReference<>();
        StockLookupDataAccessInterface lookupData = ticker -> {
            submittedTicker.set(ticker);
            return new StockLookupDataAccessInterface.Result(snapshot, "");
        };
        DashboardStockDataAccessInterface dashboardData = new DashboardStockDataAccessInterface() {
            @Override
            public Security findSecurity(String ticker) {
                return apple;
            }

            @Override
            public double getCurrentPrice(String ticker) {
                return quote.getClose();
            }
        };

        CountDownLatch rendered = new CountDownLatch(1);
        AtomicReference<StockLookupView> view = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            view.set(StockLookupApp.createView(lookupData, dashboardData, new InMemoryPortfolioRepository(), "diego"));
            JButton add = find(view.get(), JButton.class, button -> button.getText().equals("Add to portfolio"));
            assertFalse(add.isEnabled());
            add.addPropertyChangeListener("enabled", event -> {
                if (Boolean.TRUE.equals(event.getNewValue())) {
                    rendered.countDown();
                }
            });

            find(view.get(), JTextField.class, field -> true).setText(" aapl ");
            find(view.get(), JButton.class, button -> button.getText().equals("Look up")).doClick();
        });

        assertTrue(rendered.await(5, TimeUnit.SECONDS));
        assertEquals("AAPL", submittedTicker.get());
        SwingUtilities.invokeAndWait(() -> {
            JLabel latest = find(view.get(), JLabel.class, label -> label.getText().startsWith("Latest close:"));
            JLabel metrics = find(view.get(), JLabel.class, label -> label.getText().startsWith("P/E:"));
            assertEquals("Latest close: 190.00 (2026-01-05)", latest.getText());
            assertTrue(metrics.getText().contains("52-week low: Unavailable"));
        });
    }

    private <T extends Component> T find(Container parent, Class<T> type, Predicate<T> matches) {
        return Objects.requireNonNull(findOrNull(parent, type, matches),
                "The lookup view is missing a matching " + type.getSimpleName());
    }

    private <T extends Component> T findOrNull(Container parent, Class<T> type, Predicate<T> matches) {
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child) && matches.test(type.cast(child))) {
                return type.cast(child);
            }
            if (child instanceof Container) {
                T result = findOrNull((Container) child, type, matches);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
