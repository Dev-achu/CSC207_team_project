package use_case.dashboard;

/**
 * Output boundary for the Portfolio Dashboard use case.
 * Implemented by the presenter and called by the interactor.
 */
public interface AddTransactionOutputBoundary {

    /**
     * Called when the transaction was added successfully.
     *
     * @param outputData the updated dashboard data
     */
    void prepareSuccessView(AddTransactionOutputData outputData);

    /**
     * Called when the transaction could not be added.
     *
     * @param errorMessage a message explaining what went wrong
     */
    void prepareFailView(String errorMessage);
}
