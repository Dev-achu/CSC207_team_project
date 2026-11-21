package use_case;

import entity.Benchmark;

import java.time.LocalDate;

/**
 * Data access interface for loading Consumer Price Index (CPI) inflation series.
 * Lives in the use case layer so that interactors depend on this interface,
 * not on a concrete API or file class (Dependency Inversion).
 */
public interface InflationDataGateway {

    /**
     * Returns the CPI series over {@code [startDate, endDate]} as a {@link Benchmark}.
     *
     * @param startDate the start of the date range (inclusive)
     * @param endDate   the end of the date range (inclusive)
     * @return the CPI benchmark series, or null if CPI data is unavailable
     */
    Benchmark getCpiSeries(LocalDate startDate, LocalDate endDate);
}
