package interface_adapter.dashboard;

import use_case.dashboard.AddTransactionOutputBoundary;
import use_case.dashboard.AddTransactionOutputData;
import use_case.dashboard.HoldingSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Turns the dashboard output data into display strings and updates the
 * DashboardViewModel.
 */
public class DashboardPresenter implements AddTransactionOutputBoundary {
    private final DashboardViewModel dashboardViewModel;

    public DashboardPresenter(DashboardViewModel dashboardViewModel) {
        this.dashboardViewModel = dashboardViewModel;
    }

    @Override
    public void prepareSuccessView(AddTransactionOutputData outputData) {
        DashboardState state = dashboardViewModel.getState();

        state.setTotalValue(formatMoney(outputData.getTotalValue()));

        Map<String, String> allocation = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : outputData.getAllocation().entrySet()) {
            allocation.put(entry.getKey(),
                    String.format(Locale.CANADA, "%.1f%%", entry.getValue() * 100));
        }
        state.setAllocation(allocation);

        List<String> rows = new ArrayList<>();
        for (HoldingSummary holding : outputData.getHoldings()) {
            rows.add(String.format(Locale.CANADA, "%s (%s): %s shares @ %s = %s",
                    holding.getTicker(), holding.getAssetClass(), formatQuantity(holding.getQuantity()),
                    formatMoney(holding.getPrice()), formatMoney(holding.getMarketValue())));
        }
        state.setHoldingRows(rows);

        if (outputData.getStalePriceTickers().isEmpty()) {
            state.setWarning("");
        } else {
            state.setWarning("Could not fetch current prices for "
                    + String.join(", ", outputData.getStalePriceTickers())
                    + "; showing last known price.");
        }
        state.setError("");

        dashboardViewModel.setState(state);
        dashboardViewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        DashboardState state = dashboardViewModel.getState();
        state.setError(errorMessage);
        dashboardViewModel.setState(state);
        dashboardViewModel.firePropertyChanged();
    }

    private static String formatMoney(double amount) {
        return String.format(Locale.CANADA, "$%,.2f", amount);
    }

    private static String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}
