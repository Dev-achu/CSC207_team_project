package use_case.dashboard;

import entity.AssetClass;
import entity.Holding;
import entity.Portfolio;
import entity.Security;
import entity.Transaction;
import entity.TransactionType;
import use_case.PortfolioRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC2 Portfolio Dashboard: adds a buy or sell transaction to the user's
 * portfolio, then reports the portfolio's total value and allocation by
 * asset class.
 *
 * <p>Error cases: unknown ticker, selling more than is owned, non-positive
 * quantity or price, and a failed price fetch (handled by falling back to the
 * last traded price rather than failing).</p>
 */
public class AddTransactionInteractor implements AddTransactionInputBoundary {
    private final PortfolioRepository portfolioRepository;
    private final DashboardStockDataAccessInterface stockDataAccess;
    private final AddTransactionOutputBoundary presenter;

    public AddTransactionInteractor(PortfolioRepository portfolioRepository,
                                    DashboardStockDataAccessInterface stockDataAccess,
                                    AddTransactionOutputBoundary presenter) {
        this.portfolioRepository = portfolioRepository;
        this.stockDataAccess = stockDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(AddTransactionInputData inputData) {
        // 1. Check the simple input rules before touching any data.
        if (inputData.getQuantity() <= 0) {
            presenter.prepareFailView("Quantity must be greater than 0.");
            return;
        }
        if (inputData.getPrice() <= 0) {
            presenter.prepareFailView("Price must be greater than 0.");
            return;
        }
        if (inputData.getDate() == null) {
            presenter.prepareFailView("Please enter a trade date.");
            return;
        }

        // 2. Look up the ticker.
        String ticker = inputData.getTicker() == null ? "" : inputData.getTicker().trim().toUpperCase();
        Security security = stockDataAccess.findSecurity(ticker);
        if (security == null) {
            presenter.prepareFailView("Unknown ticker: " + ticker);
            return;
        }

        // 3. Load the user's portfolio, or start a new one.
        Portfolio portfolio = portfolioRepository.findByOwner(inputData.getUsername());
        if (portfolio == null) {
            portfolio = new Portfolio(UUID.randomUUID().toString(), inputData.getUsername(), "My Portfolio");
        }

        // 4. A user cannot sell shares they do not own.
        if (inputData.getType() == TransactionType.SELL
                && inputData.getQuantity() > portfolio.getQuantityOwned(ticker)) {
            presenter.prepareFailView("Cannot sell " + inputData.getQuantity() + " " + ticker
                    + ": you only own " + portfolio.getQuantityOwned(ticker) + ".");
            return;
        }

        // 5. Record the transaction and save.
        Transaction transaction = new Transaction(UUID.randomUUID().toString(), security,
                inputData.getType(), inputData.getQuantity(), inputData.getPrice(), inputData.getDate());
        portfolio.addTransaction(transaction);
        portfolioRepository.save(portfolio);

        // 6. Get a current price for every holding, falling back to the last traded price.
        Map<String, Double> prices = new HashMap<>();
        List<String> stalePriceTickers = new ArrayList<>();
        for (Holding holding : portfolio.getHoldings()) {
            String heldTicker = holding.getSecurity().getTicker();
            try {
                prices.put(heldTicker, stockDataAccess.getCurrentPrice(heldTicker));
            } catch (PriceUnavailableException e) {
                prices.put(heldTicker, portfolio.getLastTradePrice(heldTicker));
                stalePriceTickers.add(heldTicker);
            }
        }

        // 7. Build the output data and send it to the presenter.
        List<HoldingSummary> summaries = new ArrayList<>();
        for (Holding holding : portfolio.getHoldings()) {
            String heldTicker = holding.getSecurity().getTicker();
            double price = prices.get(heldTicker);
            summaries.add(new HoldingSummary(heldTicker, holding.getSecurity().getAssetClass().name(),
                    holding.getQuantity(), price, holding.getMarketValue(price)));
        }
        double totalValue = portfolio.getTotalValue(prices);
        Map<String, Double> allocation = new LinkedHashMap<>();
        for (Map.Entry<AssetClass, Double> entry : portfolio.getAllocation(prices).entrySet()) {
            allocation.put(entry.getKey().name(), entry.getValue());
        }

        presenter.prepareSuccessView(
                new AddTransactionOutputData(totalValue, allocation, summaries, stalePriceTickers));
    }
}
