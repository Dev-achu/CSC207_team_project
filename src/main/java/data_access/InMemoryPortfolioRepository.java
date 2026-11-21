package data_access;

import entity.Portfolio;
import use_case.PortfolioRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps portfolios in memory only (nothing is written to disk).
 * Used for tests and for running the app before real persistence is added.
 */
public class InMemoryPortfolioRepository implements PortfolioRepository {
    private final Map<String, Portfolio> portfoliosByOwner = new HashMap<>();

    @Override
    public Portfolio findByOwner(String username) {
        return portfoliosByOwner.get(username);
    }

    @Override
    public void save(Portfolio portfolio) {
        portfoliosByOwner.put(portfolio.getOwnerUsername(), portfolio);
    }
}
