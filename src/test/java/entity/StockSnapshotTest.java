package entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockSnapshotTest {
    private static final Security APPLE = new Security("AAPL", "Apple Inc.", AssetClass.STOCK);
    private static final LocalDate DATE = LocalDate.of(2026, 1, 5);

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.POSITIVE_INFINITY})
    void pricePointRequiresPositiveFiniteClose(double price) {
        assertThrows(IllegalArgumentException.class, () -> new PricePoint(APPLE, DATE, price));
    }

    @Test
    void snapshotKeepsItsHistoryWhenTheSourceListChanges() {
        PricePoint quote = new PricePoint(APPLE, DATE, 180);
        StockMetrics metrics = new StockMetrics(25.0, null, 220.0);
        List<PricePoint> source = new ArrayList<>(List.of(quote));

        StockSnapshot snapshot = new StockSnapshot(quote, source, metrics);
        source.clear();

        assertSame(quote, snapshot.getQuote());
        assertSame(APPLE, quote.getSecurity());
        assertEquals(DATE, quote.getDate());
        assertEquals(180, quote.getClose());
        assertEquals(List.of(quote), snapshot.getHistory());
        assertSame(metrics, snapshot.getMetrics());
        assertEquals(25.0, metrics.getPriceToEarnings().orElseThrow());
        assertFalse(metrics.getWeek52Low().isPresent());
        assertEquals(220.0, metrics.getWeek52High().orElseThrow());
    }

}
