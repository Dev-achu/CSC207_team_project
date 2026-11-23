package entity;

import java.time.LocalDate;
import java.util.Objects;

/** A closing price for one security on a trading date. */
public final class PricePoint {
    private final Security security;
    private final LocalDate date;
    private final double close;

    public PricePoint(Security security, LocalDate date, double close) {
        if (!Double.isFinite(close) || close <= 0) {
            throw new IllegalArgumentException("Closing price must be positive and finite.");
        }
        this.security = Objects.requireNonNull(security);
        this.date = Objects.requireNonNull(date);
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
