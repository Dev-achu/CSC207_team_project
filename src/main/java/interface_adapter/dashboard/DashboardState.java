package interface_adapter.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the dashboard View displays, already formatted as strings.
 */
public class DashboardState {
    private String totalValue = "$0.00";
    private Map<String, String> allocation = new LinkedHashMap<>();
    private List<String> holdingRows = new ArrayList<>();
    private String warning = "";
    private String error = "";

    /** Returns the total portfolio value, e.g. "$1,234.50". */
    public String getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(String totalValue) {
        this.totalValue = totalValue;
    }

    /** Returns asset class name to percentage, e.g. "STOCK" to "60.0%". */
    public Map<String, String> getAllocation() {
        return allocation;
    }

    public void setAllocation(Map<String, String> allocation) {
        this.allocation = allocation;
    }

    /** Returns one display line per holding. */
    public List<String> getHoldingRows() {
        return holdingRows;
    }

    public void setHoldingRows(List<String> holdingRows) {
        this.holdingRows = holdingRows;
    }

    /** Returns a warning to show the user (e.g. stale prices), or "" if none. */
    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    /** Returns an error message for the last action, or "" if it succeeded. */
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
