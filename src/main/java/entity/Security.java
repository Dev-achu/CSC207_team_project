package entity;

import java.util.Objects;

/**
 * A tradable security, such as a stock or a bond ETF.
 * Two securities are equal when they have the same ticker.
 */
public class Security {
    private final String ticker;
    private final String name;
    private final AssetClass assetClass;

    /**
     * Creates a security.
     *
     * @param ticker     the ticker symbol, e.g. "AAPL"; stored in upper case
     * @param name       the full name, e.g. "Apple Inc."
     * @param assetClass the asset class this security belongs to
     * @throws IllegalArgumentException if ticker is null or blank, or assetClass is null
     */
    public Security(String ticker, String name, AssetClass assetClass) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker must not be empty.");
        }
        if (assetClass == null) {
            throw new IllegalArgumentException("Asset class must not be null.");
        }
        this.ticker = ticker.trim().toUpperCase();
        this.name = name;
        this.assetClass = assetClass;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Security)) {
            return false;
        }
        Security other = (Security) o;
        return ticker.equals(other.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker);
    }

    @Override
    public String toString() {
        return ticker;
    }
}
