package interface_adapter.dashboard;

import org.junit.jupiter.api.Test;
import use_case.dashboard.AddTransactionInputBoundary;
import use_case.dashboard.AddTransactionInputData;
import use_case.dashboard.AddTransactionOutputData;
import use_case.dashboard.HoldingSummary;
import entity.TransactionType;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardPresenterTest {

    @Test
    void successViewFormatsValuesAndNotifiesListeners() {
        DashboardViewModel viewModel = new DashboardViewModel();
        int[] notifications = {0};
        viewModel.addPropertyChangeListener(evt -> notifications[0]++);
        DashboardPresenter presenter = new DashboardPresenter(viewModel);

        Map<String, Double> allocation = new LinkedHashMap<>();
        allocation.put("STOCK", 0.6);
        allocation.put("BOND", 0.4);
        List<HoldingSummary> holdings = List.of(
                new HoldingSummary("AAPL", "STOCK", 10, 150, 1500));
        presenter.prepareSuccessView(new AddTransactionOutputData(
                2500, allocation, holdings, List.of("AAPL")));

        DashboardState state = viewModel.getState();
        assertEquals("$2,500.00", state.getTotalValue());
        assertEquals("60.0%", state.getAllocation().get("STOCK"));
        assertEquals("40.0%", state.getAllocation().get("BOND"));
        assertEquals("AAPL (STOCK): 10 shares @ $150.00 = $1,500.00", state.getHoldingRows().get(0));
        assertTrue(state.getWarning().contains("AAPL"));
        assertEquals("", state.getError());
        assertEquals(1, notifications[0]);
    }

    @Test
    void failViewSetsErrorAndKeepsOldDashboard() {
        DashboardViewModel viewModel = new DashboardViewModel();
        DashboardPresenter presenter = new DashboardPresenter(viewModel);
        presenter.prepareSuccessView(new AddTransactionOutputData(
                100, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList()));

        presenter.prepareFailView("Unknown ticker: ZZZZ");

        assertEquals("Unknown ticker: ZZZZ", viewModel.getState().getError());
        assertEquals("$100.00", viewModel.getState().getTotalValue());
    }

    @Test
    void controllerConvertsTypeAndPassesInputData() {
        AddTransactionInputData[] received = new AddTransactionInputData[1];
        AddTransactionInputBoundary fakeInteractor = inputData -> received[0] = inputData;
        AddTransactionController controller = new AddTransactionController(fakeInteractor);

        controller.execute("ryan", "AAPL", "sell", 2, 150, LocalDate.of(2026, 11, 20));

        assertEquals(TransactionType.SELL, received[0].getType());
        assertEquals("AAPL", received[0].getTicker());
        assertEquals(2, received[0].getQuantity());
    }
}
