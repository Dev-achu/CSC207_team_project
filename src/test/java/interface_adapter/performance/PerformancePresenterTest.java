package interface_adapter.performance;

import org.junit.jupiter.api.Test;
import use_case.performance.ComparisonPoint;
import use_case.performance.PerformanceInputBoundary;
import use_case.performance.PerformanceInputData;
import use_case.performance.PerformanceOutputData;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformancePresenterTest {

    @Test
    void successViewFormatsReturnsSummaryAndChartRowsAndNotifiesListeners() {
        PerformanceViewModel viewModel = new PerformanceViewModel();
        int[] notifications = {0};
        viewModel.addPropertyChangeListener(evt -> notifications[0]++);
        PerformancePresenter presenter = new PerformancePresenter(viewModel);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 1);
        List<ComparisonPoint> series = List.of(
                new ComparisonPoint(start, 0.0, 0.0, 0.0),
                new ComparisonPoint(end, 0.20, 0.10, 0.03));

        presenter.prepareSuccessView(new PerformanceOutputData(
                "My Portfolio", "S&P 500", start, end,
                0.20, 0.165048, 0.10, 0.067961, 0.03,
                true, true, series));

        PerformanceState state = viewModel.getState();
        assertEquals("My Portfolio", state.getPortfolioName());
        assertEquals("S&P 500", state.getBenchmarkName());
        assertEquals("2026-01-01 to 2026-06-01", state.getDateRange());
        assertEquals("20.00%", state.getPortfolioNominalReturn());
        assertEquals("16.50%", state.getPortfolioRealReturn());
        assertEquals("10.00%", state.getBenchmarkNominalReturn());
        assertEquals("6.80%", state.getBenchmarkRealReturn());
        assertEquals("3.00%", state.getInflationRate());
        assertTrue(state.isBeatingBenchmark());
        assertTrue(state.isBeatingInflation());
        assertTrue(state.getSummary().contains("beat S&P 500"));
        assertTrue(state.getSummary().contains("beat inflation"));
        assertEquals(2, state.getChartRows().size());
        assertEquals("2026-06-01 | Portfolio: 20.00% | S&P 500: 10.00% | CPI: 3.00%",
                state.getChartRows().get(1));
        assertEquals("", state.getError());
        assertEquals(1, notifications[0]);
    }

    @Test
    void failViewSetsErrorAndKeepsPreviousState() {
        PerformanceViewModel viewModel = new PerformanceViewModel();
        PerformancePresenter presenter = new PerformancePresenter(viewModel);
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 1);

        presenter.prepareSuccessView(new PerformanceOutputData(
                "My Portfolio", "TSX", start, end,
                0.12, 0.087379, 0.05, 0.019417, 0.03,
                true, true, List.of()));
        presenter.prepareFailView("Start date must be before end date.");

        assertEquals("Start date must be before end date.", viewModel.getState().getError());
        assertEquals("12.00%", viewModel.getState().getPortfolioNominalReturn());
    }

    @Test
    void controllerPackagesAndForwardsInputData() {
        PerformanceInputData[] received = new PerformanceInputData[1];
        PerformanceInputBoundary fakeInteractor = inputData -> received[0] = inputData;
        PerformanceController controller = new PerformanceController(fakeInteractor);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 1);
        controller.execute("achu", start, end, "S&P 500");

        assertEquals("achu", received[0].getUsername());
        assertEquals(start, received[0].getStartDate());
        assertEquals(end, received[0].getEndDate());
        assertEquals("S&P 500", received[0].getBenchmarkSymbol());
    }
}
