package data_access;

import entity.Benchmark;
import entity.PricePoint;
import use_case.InflationDataGateway;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps CPI inflation data points in memory only.
 * Used for tests and for running the app before a live CPI data source is wired in.
 */
public class InMemoryInflationDataGateway implements InflationDataGateway {
    private final List<PricePoint> cpiPoints = new ArrayList<>();

    /**
     * Adds a single CPI observation on the given date.
     *
     * @param date     the CPI observation date
     * @param cpiValue the CPI index value (must be greater than 0)
     */
    public void addCpiPoint(LocalDate date, double cpiValue) {
        cpiPoints.add(new PricePoint(date, cpiValue));
    }

    /**
     * Clears all stored CPI points.
     */
    public void clear() {
        cpiPoints.clear();
    }

    @Override
    public Benchmark getCpiSeries(LocalDate startDate, LocalDate endDate) {
        if (cpiPoints.isEmpty()) {
            return null;
        }
        List<PricePoint> inRange = new ArrayList<>();
        for (PricePoint point : cpiPoints) {
            if (!point.getDate().isBefore(startDate) && !point.getDate().isAfter(endDate)) {
                inRange.add(point);
            }
        }
        if (inRange.isEmpty()) {
            return null;
        }
        return new Benchmark("CPI", "CPI", inRange);
    }
}
