package use_case.dashboard;

import java.util.List;
import java.util.Map;

/**
 * The result of adding a transaction: the updated dashboard data.
 * Created by the interactor and passed to the presenter.
 */
public class AddTransactionOutputData {
    private final double totalValue;
    private final Map<String, Double> allocation;
    private final List<HoldingSummary> holdings;
    private final List<String> stalePriceTickers;

    /**
     * Creates the output data.
     *
     * @param totalValue        the total market value of the portfolio
     * @param allocation        the fraction (0 to 1) of value in each asset class, keyed by asset class name
     * @param holdings          one summary per holding
     * @param stalePriceTickers tickers whose current price could not be fetched,
     *                          so their last traded price was used instead
     */
    public AddTransactionOutputData(double totalValue, Map<String, Double> allocation,
                                    List<HoldingSummary> holdings, List<String> stalePriceTickers) {
        this.totalValue = totalValue;
        this.allocation = allocation;
        this.holdings = holdings;
        this.stalePriceTickers = stalePriceTickers;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public Map<String, Double> getAllocation() {
        return allocation;
    }

    public List<HoldingSummary> getHoldings() {
        return holdings;
    }

    public List<String> getStalePriceTickers() {
        return stalePriceTickers;
    }
}
