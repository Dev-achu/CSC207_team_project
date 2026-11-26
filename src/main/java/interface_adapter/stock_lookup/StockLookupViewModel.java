package interface_adapter.stock_lookup;

import interface_adapter.ViewModel;

/** Observable state for the stock lookup view. */
public final class StockLookupViewModel extends ViewModel<StockLookupState> {
    /** Starts with empty display state until a lookup finishes. */
    public StockLookupViewModel() {
        super("Stock Lookup");
        setState(StockLookupState.empty(""));
    }
}
