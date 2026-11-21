package use_case.dashboard;

/**
 * One row of the dashboard: a holding with the price used to value it.
 * Part of the output data, so the presenter never needs to touch entities.
 */
public class HoldingSummary {
    private final String ticker;
    private final String assetClass;
    private final double quantity;
    private final double price;
    private final double marketValue;

    public HoldingSummary(String ticker, String assetClass, double quantity,
                          double price, double marketValue) {
        this.ticker = ticker;
        this.assetClass = assetClass;
        this.quantity = quantity;
        this.price = price;
        this.marketValue = marketValue;
    }

    public String getTicker() {
        return ticker;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getMarketValue() {
        return marketValue;
    }
}
