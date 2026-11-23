package interface_adapter.stock_lookup;

import interface_adapter.ViewModel;

/** Observable state for the stock lookup view. */
public final class StockLookupViewModel extends ViewModel<StockLookupState> {
    public StockLookupViewModel() {
        super("Stock Lookup");
        setState(StockLookupState.empty(""));
    }
}
