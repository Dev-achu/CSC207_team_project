package interface_adapter.performance;

import interface_adapter.ViewModel;

/**
 * The ViewModel for the Performance vs. Benchmark & Inflation view.
 */
public class PerformanceViewModel extends ViewModel<PerformanceState> {

    public PerformanceViewModel() {
        super("performance");
        setState(new PerformanceState());
    }
}
