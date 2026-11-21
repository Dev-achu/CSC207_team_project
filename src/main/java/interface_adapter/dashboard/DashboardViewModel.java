package interface_adapter.dashboard;

import interface_adapter.ViewModel;

/**
 * The ViewModel for the Portfolio Dashboard view.
 */
public class DashboardViewModel extends ViewModel<DashboardState> {

    public DashboardViewModel() {
        super("dashboard");
        setState(new DashboardState());
    }
}
