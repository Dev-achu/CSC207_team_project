package use_case.dashboard;

import entity.Security;

/**
 * The market data the Portfolio Dashboard use case needs: looking up a
 * ticker and getting its current price. Kept small on purpose (Interface
 * Segregation). The Alpha Vantage adapter implements this port so the
 * dashboard and stock lookup can reuse cached quotes.
 */
public interface DashboardStockDataAccessInterface {

    /**
     * Looks up a security by ticker.
     *
     * @param ticker the ticker symbol in upper case
     * @return the matching security, or null if the ticker is unknown
     */
    Security findSecurity(String ticker);

    /**
     * Returns the current price per share of a security.
     *
     * @param ticker the ticker symbol in upper case
     * @return the current price
     * @throws PriceUnavailableException if the price cannot be fetched right now
     */
    double getCurrentPrice(String ticker) throws PriceUnavailableException;
}
