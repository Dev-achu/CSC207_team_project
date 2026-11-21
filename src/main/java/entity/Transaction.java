package entity;

import java.time.LocalDate;

/**
 * A single buy or sell of a security. Transactions are immutable.
 */
public class Transaction {
    private final String id;
    private final Security security;
    private final TransactionType type;
    private final double quantity;
    private final double price;
    private final LocalDate date;

    /**
     * Creates a transaction.
     *
     * @param id       a unique id for this transaction
     * @param security the security that was bought or sold
     * @param type     BUY or SELL
     * @param quantity the number of shares; must be greater than 0
     * @param price    the price per share; must be greater than 0
     * @param date     the trade date
     * @throws IllegalArgumentException if any argument is null, or quantity or price is not positive
     */
    public Transaction(String id, Security security, TransactionType type,
                       double quantity, double price, LocalDate date) {
        if (security == null || type == null || date == null) {
            throw new IllegalArgumentException("Security, type and date are required.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
        this.id = id;
        this.security = security;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public Security getSecurity() {
        return security;
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
