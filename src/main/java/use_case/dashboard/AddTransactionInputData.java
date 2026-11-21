package use_case.dashboard;

import entity.TransactionType;

import java.time.LocalDate;

/**
 * The input for adding a transaction: created by the controller and passed
 * to the interactor through the input boundary.
 */
public class AddTransactionInputData {
    private final String username;
    private final String ticker;
    private final TransactionType type;
    private final double quantity;
    private final double price;
    private final LocalDate date;

    public AddTransactionInputData(String username, String ticker, TransactionType type,
                                   double quantity, double price, LocalDate date) {
        this.username = username;
        this.ticker = ticker;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public String getTicker() {
        return ticker;
    }

    public TransactionType getType() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public LocalDate getDate() {
        return date;
    }
}
