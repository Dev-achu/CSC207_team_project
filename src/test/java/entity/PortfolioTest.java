package entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortfolioTest {
    private static final double DELTA = 1e-9;
    private final Security apple = new Security("AAPL", "Apple Inc.", AssetClass.STOCK);
    private final Security bonds = new Security("BND", "Vanguard Total Bond Market ETF", AssetClass.BOND);
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio("p1", "ryan", "My Portfolio");
    }

    private Transaction buy(Security security, double quantity, double price, LocalDate date) {
        return new Transaction("t", security, TransactionType.BUY, quantity, price, date);
    }

    private Transaction sell(Security security, double quantity, double price, LocalDate date) {
        return new Transaction("t", security, TransactionType.SELL, quantity, price, date);
    }

    @Test
    void buyingTwiceUpdatesQuantityAndAverageCost() {
        portfolio.addTransaction(buy(apple, 10, 100, LocalDate.of(2026, 1, 1)));
        portfolio.addTransaction(buy(apple, 10, 200, LocalDate.of(2026, 2, 1)));

        Holding holding = portfolio.getHoldings().get(0);
        assertEquals(20, holding.getQuantity(), DELTA);
        assertEquals(150, holding.getAverageCost(), DELTA);
    }

    @Test
    void sellingAllSharesRemovesTheHolding() {
        portfolio.addTransaction(buy(apple, 5, 100, LocalDate.of(2026, 1, 1)));
        portfolio.addTransaction(sell(apple, 5, 120, LocalDate.of(2026, 2, 1)));

        assertTrue(portfolio.getHoldings().isEmpty());
        assertEquals(2, portfolio.getTransactions().size());
    }

    @Test
    void sellingMoreThanOwnedThrows() {
        portfolio.addTransaction(buy(apple, 5, 100, LocalDate.of(2026, 1, 1)));

        assertThrows(IllegalArgumentException.class,
                () -> portfolio.addTransaction(sell(apple, 6, 100, LocalDate.of(2026, 2, 1))));
        assertEquals(5, portfolio.getQuantityOwned("AAPL"), DELTA);
    }

    @Test
    void totalValueAndAllocationUseCurrentPrices() {
        portfolio.addTransaction(buy(apple, 10, 100, LocalDate.of(2026, 1, 1)));
        portfolio.addTransaction(buy(bonds, 20, 50, LocalDate.of(2026, 1, 1)));
        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 150.0);  // 10 * 150 = 1500
        prices.put("BND", 50.0);    // 20 * 50  = 1000

        assertEquals(2500, portfolio.getTotalValue(prices), DELTA);
        Map<AssetClass, Double> allocation = portfolio.getAllocation(prices);
        assertEquals(0.6, allocation.get(AssetClass.STOCK), DELTA);
        assertEquals(0.4, allocation.get(AssetClass.BOND), DELTA);
    }

    @Test
    void emptyPortfolioHasNoAllocation() {
        assertEquals(0, portfolio.getTotalValue(new HashMap<>()), DELTA);
        assertTrue(portfolio.getAllocation(new HashMap<>()).isEmpty());
    }

    @Test
    void lastTradePriceIsFromTheMostRecentDate() {
        portfolio.addTransaction(buy(apple, 1, 100, LocalDate.of(2026, 3, 1)));
        portfolio.addTransaction(buy(apple, 1, 90, LocalDate.of(2026, 1, 1)));

        assertEquals(100, portfolio.getLastTradePrice("AAPL"), DELTA);
        assertEquals(0, portfolio.getLastTradePrice("MSFT"), DELTA);
    }

    @Test
    void transactionRejectsNonPositiveQuantityOrPrice() {
        assertThrows(IllegalArgumentException.class, () -> buy(apple, 0, 100, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> buy(apple, 1, -5, LocalDate.now()));
    }

    @Test
    void securityTickerIsStoredInUpperCase() {
        assertEquals("AAPL", new Security(" aapl ", "Apple Inc.", AssetClass.STOCK).getTicker());
    }
}
