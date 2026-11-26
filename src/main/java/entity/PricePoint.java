package entity;

import java.time.LocalDate;

/**
 * A single closing price (or index level) on a specific date.
 * Used for both individual security price history and benchmark / CPI series.
 */
public class PricePoint {
    private final Security security;
    private final LocalDate date;
    private final double close;

    /**
     * Creates a price point for a specific security.
     *
     * @param security the security this price belongs to
     * @param date     the observation date
     * @param close    the closing price on that date; must be greater than 0
     * @throws IllegalArgumentException if security or date is null, or close is not positive
     */
    public PricePoint(Security security, LocalDate date, double close) {
        if (security == null) {
            throw new IllegalArgumentException("Security must not be null.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null.");
        }
        if (close <= 0) {
            throw new IllegalArgumentException("Close price must be greater than 0.");
        }
        this.security = security;
        this.date = date;
        this.close = close;
    }

    /**
     * Creates a price point for a benchmark or index series (such as S&P 500, TSX, or CPI)
     * that is not tied to a tradable {@link Security}.
     *
     * @param date  the observation date
     * @param close the closing index level on that date; must be greater than 0
     * @throws IllegalArgumentException if date is null or close is not positive
     */
    public PricePoint(LocalDate date, double close) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null.");
        }
        if (close <= 0) {
            throw new IllegalArgumentException("Close price must be greater than 0.");
        }
        this.security = null;
        this.date = date;
        this.close = close;
    }

    public Security getSecurity() {
        return security;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getClose() {
        return close;
    }
}
