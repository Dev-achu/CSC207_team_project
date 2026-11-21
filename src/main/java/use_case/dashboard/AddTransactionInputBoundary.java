package use_case.dashboard;

/**
 * Input boundary for the Portfolio Dashboard use case.
 * Implemented by the interactor and called by the controller.
 */
public interface AddTransactionInputBoundary {

    /**
     * Adds a transaction to the user's portfolio and reports the updated
     * dashboard (or an error) through the output boundary.
     *
     * @param inputData the transaction details
     */
    void execute(AddTransactionInputData inputData);
}
