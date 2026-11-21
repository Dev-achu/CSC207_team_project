package interface_adapter.dashboard;

import entity.TransactionType;
import use_case.dashboard.AddTransactionInputBoundary;
import use_case.dashboard.AddTransactionInputData;

import java.time.LocalDate;

/**
 * Receives "add transaction" requests from the View, packages them as
 * input data, and starts the Portfolio Dashboard use case.
 */
public class AddTransactionController {
    private final AddTransactionInputBoundary addTransactionInteractor;

    public AddTransactionController(AddTransactionInputBoundary addTransactionInteractor) {
        this.addTransactionInteractor = addTransactionInteractor;
    }

    /**
     * Adds a transaction for the logged-in user.
     *
     * @param username the logged-in user's username
     * @param ticker   the ticker typed by the user
     * @param type     "BUY" or "SELL" (any case)
     * @param quantity the number of shares
     * @param price    the price per share
     * @param date     the trade date
     */
    public void execute(String username, String ticker, String type,
                        double quantity, double price, LocalDate date) {
        TransactionType transactionType = TransactionType.valueOf(type.trim().toUpperCase());
        AddTransactionInputData inputData = new AddTransactionInputData(
                username, ticker, transactionType, quantity, price, date);
        addTransactionInteractor.execute(inputData);
    }
}
