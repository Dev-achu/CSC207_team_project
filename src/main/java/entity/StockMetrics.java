package entity;

import java.util.OptionalDouble;

/** Fundamentals can be absent individually, especially for funds. */
public final class StockMetrics {
    private final Double priceToEarnings;
    private final Double week52Low;
    private final Double week52High;

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

    private OptionalDouble optional(Double value) {
        return value == null ? OptionalDouble.empty() : OptionalDouble.of(value);
    }
}
