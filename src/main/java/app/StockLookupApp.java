package app;

import data_access.AlphaVantageStockDataAccess;
import data_access.InMemoryPortfolioRepository;
import interface_adapter.dashboard.AddTransactionController;
import interface_adapter.dashboard.DashboardPresenter;
import interface_adapter.dashboard.DashboardViewModel;
import interface_adapter.stock_lookup.StockLookupController;
import interface_adapter.stock_lookup.StockLookupPresenter;
import interface_adapter.stock_lookup.StockLookupViewModel;
import use_case.PortfolioRepository;
import use_case.dashboard.AddTransactionInteractor;
import use_case.dashboard.DashboardStockDataAccessInterface;
import use_case.stock_lookup.StockLookupDataAccessInterface;
import use_case.stock_lookup.StockLookupInteractor;
import view.StockLookupView;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Assembles the standalone stock lookup screen and its portfolio flow. */
public final class StockLookupApp {
    private static final String API_KEY_NAME = "ALPHAVANTAGE_API_KEY";

    private StockLookupApp() {
    }

    /** Loads the provider configuration and opens the demo on Swing's event thread. */
    public static void main(String[] args) throws IOException {
        AlphaVantageStockDataAccess dataAccess = new AlphaVantageStockDataAccess(readApiKey());
        SwingUtilities.invokeLater(() -> {
            StockLookupView view = createView(dataAccess, dataAccess, new InMemoryPortfolioRepository(), "demo");
            JFrame window = new JFrame("Stock Lookup - Demo portfolio");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(view);
            window.pack();
            window.setMinimumSize(window.getSize());
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }

    /** Composes the screen for a portfolio owner supplied by the application. */
    public static StockLookupView createView(StockLookupDataAccessInterface stockData,
                                             DashboardStockDataAccessInterface dashboardData,
                                             PortfolioRepository portfolios, String username) {
        StockLookupViewModel lookupModel = new StockLookupViewModel();
        StockLookupPresenter lookupPresenter = new StockLookupPresenter(lookupModel);
        StockLookupInteractor lookup = new StockLookupInteractor(stockData, lookupPresenter);
        StockLookupController lookupController = new StockLookupController(lookup);

        DashboardViewModel dashboardModel = new DashboardViewModel();
        DashboardPresenter dashboardPresenter = new DashboardPresenter(dashboardModel);
        AddTransactionInteractor addTransaction = new AddTransactionInteractor(
                portfolios, dashboardData, dashboardPresenter);
        AddTransactionController transactionController = new AddTransactionController(addTransaction);

        return new StockLookupView(lookupController, lookupModel, transactionController, dashboardModel, username);
    }

    /** Checks the environment first, then the local .env file. */
    private static String readApiKey() throws IOException {
        String environmentKey = System.getenv(API_KEY_NAME);
        if (environmentKey != null && !environmentKey.isBlank()) {
            return environmentKey.strip();
        }

        Path envFile = Path.of(".env");
        if (Files.isRegularFile(envFile)) {
            for (String line : Files.readAllLines(envFile)) {
                String[] entry = line.split("=", 2);
                if (entry.length == 2 && API_KEY_NAME.equals(entry[0].strip())) {
                    String value = entry[1].strip();
                    if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                            || value.startsWith("'") && value.endsWith("'"))) {
                        return value.substring(1, value.length() - 1);
                    }
                    return value;
                }
            }
        }
        return "";
    }
}
