package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A user's portfolio: the list of transactions they have entered, and the
 * holdings that result from them. A portfolio can compute its total value and
 * its allocation by asset class, given current prices.
 */
public class Portfolio {
    private final String id;
    private final String ownerUsername;
    private final String name;
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Holding> holdings = new LinkedHashMap<>();

    /**
     * Creates an empty portfolio.
     *
     * @param id            a unique id for this portfolio
     * @param ownerUsername the username of the user who owns this portfolio
     * @param name          a display name, e.g. "My Portfolio"
     */
    public Portfolio(String id, String ownerUsername, String name) {
        this.id = id;
        this.ownerUsername = ownerUsername;
        this.name = name;
    }

    /**
     * Adds a transaction and updates the matching holding.
     * A holding whose quantity drops to 0 is removed.
     *
     * @param transaction the transaction to add
     * @throws IllegalArgumentException if the transaction sells more shares than are owned
     */
    public void addTransaction(Transaction transaction) {
        String ticker = transaction.getSecurity().getTicker();
        if (transaction.getType() == TransactionType.SELL) {
            if (transaction.getQuantity() > getQuantityOwned(ticker)) {
                throw new IllegalArgumentException("Cannot sell more " + ticker + " than is owned.");
            }
            Holding holding = holdings.get(ticker);
            holding.removeShares(transaction.getQuantity());
            if (holding.getQuantity() == 0) {
                holdings.remove(ticker);
            }
        } else {
            Holding holding = holdings.get(ticker);
            if (holding == null) {
                holding = new Holding(transaction.getSecurity());
                holdings.put(ticker, holding);
            }
            holding.addShares(transaction.getQuantity(), transaction.getPrice());
        }
        transactions.add(transaction);
    }

    /**
     * Returns how many shares of the given ticker this portfolio owns.
     *
     * @param ticker the ticker to look up
     * @return the quantity owned, or 0 if none
     */
    public double getQuantityOwned(String ticker) {
        Holding holding = holdings.get(ticker);
        if (holding == null) {
            return 0;
        }
        return holding.getQuantity();
    }

    /**
     * Returns the price of the most recent transaction for the given ticker.
     * Used as a fallback when a current price cannot be fetched.
     * If two transactions have the same date, the one added last wins.
     *
     * @param ticker the ticker to look up
     * @return the last traded price, or 0 if there are no transactions for it
     */
    public double getLastTradePrice(String ticker) {
        Transaction latest = null;
        for (Transaction t : transactions) {
            if (t.getSecurity().getTicker().equals(ticker)
                    && (latest == null || !t.getDate().isBefore(latest.getDate()))) {
                latest = t;
            }
        }
        if (latest == null) {
            return 0;
        }
        return latest.getPrice();
    }

    /**
     * Returns the total market value of all holdings.
     *
     * @param prices the current price per share, keyed by ticker; every held ticker must be present
     * @return the sum of each holding's market value
     */
    public double getTotalValue(Map<String, Double> prices) {
        double total = 0;
        for (Holding holding : holdings.values()) {
            total += holding.getMarketValue(prices.get(holding.getSecurity().getTicker()));
        }
        return total;
    }

    /**
     * Returns the fraction of the portfolio's value held in each asset class.
     * Fractions are between 0 and 1 and add up to 1. Asset classes with no
     * holdings are left out. An empty portfolio returns an empty map.
     *
     * @param prices the current price per share, keyed by ticker; every held ticker must be present
     * @return a map from asset class to its fraction of the total value
     */
    public Map<AssetClass, Double> getAllocation(Map<String, Double> prices) {
        Map<AssetClass, Double> allocation = new EnumMap<>(AssetClass.class);
        double total = getTotalValue(prices);
        if (total == 0) {
            return allocation;
        }
        for (Holding holding : holdings.values()) {
            AssetClass assetClass = holding.getSecurity().getAssetClass();
            double value = holding.getMarketValue(prices.get(holding.getSecurity().getTicker()));
            allocation.put(assetClass, allocation.getOrDefault(assetClass, 0.0) + value / total);
        }
        return allocation;
    }

    public String getId() {
        return id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the transactions in the order they were added.
     *
     * @return an unmodifiable list of transactions
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Returns the current holdings.
     *
     * @return an unmodifiable list of holdings
     */
    public List<Holding> getHoldings() {
        return Collections.unmodifiableList(new ArrayList<>(holdings.values()));
    }
}
