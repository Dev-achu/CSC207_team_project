package entity;

/**
 * How much of one security a portfolio currently owns, and the average
 * price paid per share. Holdings are derived from a portfolio's transactions.
 */
public class Holding {
    private final Security security;
    private double quantity;
    private double averageCost;

    /**
     * Creates an empty holding of the given security.
     *
     * @param security the security held
     */
    public Holding(Security security) {
        this.security = security;
        this.quantity = 0;
        this.averageCost = 0;
    }

    /**
     * Records a purchase and updates the average cost per share.
     *
     * @param boughtQuantity the number of shares bought
     * @param price          the price paid per share
     */
    void addShares(double boughtQuantity, double price) {
        double totalCost = quantity * averageCost + boughtQuantity * price;
        quantity = quantity + boughtQuantity;
        averageCost = totalCost / quantity;
    }

    /**
     * Records a sale. The average cost per share does not change when selling.
     *
     * @param soldQuantity the number of shares sold
     * @throws IllegalArgumentException if more shares are sold than are held
     */
    void removeShares(double soldQuantity) {
        if (soldQuantity > quantity) {
            throw new IllegalArgumentException("Cannot sell more " + security.getTicker()
                    + " than is owned.");
        }
        quantity = quantity - soldQuantity;
    }

    /**
     * Returns the value of this holding at the given price per share.
     *
     * @param currentPrice the current price per share
     * @return quantity times currentPrice
     */
    public double getMarketValue(double currentPrice) {
        return quantity * currentPrice;
    }

    public Security getSecurity() {
        return security;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAverageCost() {
        return averageCost;
    }
}
