package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A reference series of {@link PricePoint}s over time, such as a market index
 * (S&P 500, TSX) or the Consumer Price Index (CPI) used to measure inflation.
 */
public class Benchmark {
    private final String name;
    private final String symbol;
    private final List<PricePoint> series;

    /**
     * Creates a benchmark with its historical series of price points.
     * The series is stored sorted by date in ascending order.
     *
     * @param name   the display name, e.g. "S&P 500", "TSX", or "CPI"
     * @param symbol the symbol or identifier, e.g. "SPY", "XIU.TO", or "CPI"
     * @param series the price points for this benchmark
     * @throws IllegalArgumentException if name or symbol is null/blank, or series is null
     */
    public Benchmark(String name, String symbol, List<PricePoint> series) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Benchmark name must not be empty.");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Benchmark symbol must not be empty.");
        }
        if (series == null) {
            throw new IllegalArgumentException("Benchmark series must not be null.");
        }
        this.name = name.trim();
        this.symbol = symbol.trim().toUpperCase();
        List<PricePoint> copy = new ArrayList<>(series);
        copy.sort(Comparator.comparing(PricePoint::getDate));
        this.series = Collections.unmodifiableList(copy);
    }

    /**
     * Returns all price points in this benchmark that fall within
     * {@code [startDate, endDate]} (inclusive), ordered by date ascending.
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return an unmodifiable list of price points within the range
     */
    public List<PricePoint> getPointsInRange(LocalDate startDate, LocalDate endDate) {
        List<PricePoint> result = new ArrayList<>();
        for (PricePoint point : series) {
            if (!point.getDate().isBefore(startDate) && !point.getDate().isAfter(endDate)) {
                result.add(point);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns whether this benchmark has enough data in {@code [startDate, endDate]}
     * to compute a return (at least two price points in the range).
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return true if there are at least two price points in the range
     */
    public boolean hasDataInRange(LocalDate startDate, LocalDate endDate) {
        return getPointsInRange(startDate, endDate).size() >= 2;
    }

    /**
     * Computes the simple total return (or inflation rate, for CPI) over
     * {@code [startDate, endDate]} from the first to the last price point in the range:
     * {@code (endClose - startClose) / startClose}.
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return the fractional return over the range (e.g. 0.10 for +10%)
     * @throws IllegalArgumentException if there are fewer than two price points in the range
     */
    public double getTotalReturn(LocalDate startDate, LocalDate endDate) {
        List<PricePoint> inRange = getPointsInRange(startDate, endDate);
        if (inRange.size() < 2) {
            throw new IllegalArgumentException("Not enough data points in range for " + name + ".");
        }
        double startClose = inRange.get(0).getClose();
        double endClose = inRange.get(inRange.size() - 1).getClose();
        return (endClose - startClose) / startClose;
    }

    /**
     * Returns the closing value on the latest available date on or before {@code date}.
     * If {@code date} is earlier than all points in the series, returns the earliest point's close.
     *
     * @param date the target date
     * @return the closing value, or 0 if the series is empty
     */
    public double getCloseOnOrBefore(LocalDate date) {
        if (series.isEmpty()) {
            return 0;
        }
        PricePoint latest = series.get(0);
        for (PricePoint point : series) {
            if (!point.getDate().isAfter(date)) {
                latest = point;
            } else {
                break;
            }
        }
        return latest.getClose();
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public List<PricePoint> getSeries() {
        return series;
    }
}
