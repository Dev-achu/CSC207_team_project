package entity;

import java.util.OptionalDouble;

/** Fundamentals can be absent individually, especially for funds. */
public final class StockMetrics {
    private final Double priceToEarnings;
    private final Double week52Low;
    private final Double week52High;

    /** Stores the P/E ratio and 52-week range, allowing each metric to be missing. */
    public StockMetrics(Double priceToEarnings, Double week52Low, Double week52High) {
        this.priceToEarnings = priceToEarnings;
        this.week52Low = week52Low;
        this.week52High = week52High;
    }

    public OptionalDouble getPriceToEarnings() {
        return optional(priceToEarnings);
    }

    public OptionalDouble getWeek52Low() {
        return optional(week52Low);
    }

    public OptionalDouble getWeek52High() {
        return optional(week52High);
    }

    /** Represents a missing metric as an empty optional. */
    private OptionalDouble optional(Double value) {
        return value == null ? OptionalDouble.empty() : OptionalDouble.of(value);
    }
}
