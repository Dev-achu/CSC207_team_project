package entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchmarkTest {
    private static final double DELTA = 1e-9;

    @Test
    void pricePointStoresSecurityDateAndClose() {
        Security apple = new Security("AAPL", "Apple Inc.", AssetClass.STOCK);
        LocalDate date = LocalDate.of(2026, 1, 1);
        PricePoint point = new PricePoint(apple, date, 150.0);

        assertEquals(apple, point.getSecurity());
        assertEquals(date, point.getDate());
        assertEquals(150.0, point.getClose(), DELTA);
    }

    @Test
    void benchmarkPricePointWithoutSecurityIsAllowed() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        PricePoint point = new PricePoint(date, 5000.0);

        assertNull(point.getSecurity());
        assertEquals(date, point.getDate());
        assertEquals(5000.0, point.getClose(), DELTA);
    }

    @Test
    void pricePointRejectsInvalidInputs() {
        Security apple = new Security("AAPL", "Apple Inc.", AssetClass.STOCK);
        LocalDate date = LocalDate.of(2026, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> new PricePoint(null, date, 100.0));
        assertThrows(IllegalArgumentException.class, () -> new PricePoint(apple, null, 100.0));
        assertThrows(IllegalArgumentException.class, () -> new PricePoint(apple, date, 0));
        assertThrows(IllegalArgumentException.class, () -> new PricePoint(date, -10.0));
        assertThrows(IllegalArgumentException.class, () -> new PricePoint(null, 100.0));
    }

    @Test
    void benchmarkSortsPointsAndComputesTotalReturnInRange() {
        List<PricePoint> points = List.of(
                new PricePoint(LocalDate.of(2026, 6, 1), 4400.0),
                new PricePoint(LocalDate.of(2026, 1, 1), 4000.0),
                new PricePoint(LocalDate.of(2026, 3, 1), 4200.0));
        Benchmark sp500 = new Benchmark("S&P 500", " spy ", points);

        assertEquals("S&P 500", sp500.getName());
        assertEquals("SPY", sp500.getSymbol());
        assertEquals(LocalDate.of(2026, 1, 1), sp500.getSeries().get(0).getDate());
        assertTrue(sp500.hasDataInRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1)));
        assertEquals(0.10, sp500.getTotalReturn(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1)), DELTA);
        assertEquals(4200.0, sp500.getCloseOnOrBefore(LocalDate.of(2026, 4, 15)), DELTA);
    }

    @Test
    void benchmarkWithFewerThanTwoPointsInRangeCannotComputeReturn() {
        Benchmark cpi = new Benchmark("CPI", "CPI", List.of(
                new PricePoint(LocalDate.of(2026, 1, 1), 160.0)));

        assertFalse(cpi.hasDataInRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> cpi.getTotalReturn(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1)));
    }

    @Test
    void benchmarkRejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new Benchmark("", "SPY", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Benchmark("S&P 500", " ", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Benchmark("S&P 500", "SPY", null));
    }
}
