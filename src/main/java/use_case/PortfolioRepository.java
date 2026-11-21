package use_case;

import entity.Portfolio;

/**
 * Data access interface for loading and saving portfolios.
 * It lives in the use case layer so that interactors depend on this interface,
 * not on a concrete database or file class (Dependency Inversion).
 * Shared by the use cases that read or change a portfolio (UC2, UC4, UC5).
 */
public interface PortfolioRepository {

    /**
     * Returns the portfolio owned by the given user.
     *
     * @param username the owner's username
     * @return the user's portfolio, or null if they do not have one yet
     */
    Portfolio findByOwner(String username);

    /**
     * Saves a portfolio, replacing any earlier version with the same owner.
     *
     * @param portfolio the portfolio to save
     */
    void save(Portfolio portfolio);
}
