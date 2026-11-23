package view;

import interface_adapter.dashboard.AddTransactionController;
import interface_adapter.dashboard.DashboardState;
import interface_adapter.dashboard.DashboardViewModel;
import interface_adapter.stock_lookup.StockLookupController;
import interface_adapter.stock_lookup.StockLookupState;
import interface_adapter.stock_lookup.StockLookupViewModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException;

/** Stock lookup screen and entry point to recording a buy in the portfolio. */
public final class StockLookupView extends JPanel {
    private final StockLookupController lookupController;
    private final AddTransactionController transactionController;
    private final String username;
    private final JTextField ticker = new JTextField(12);
    private final JButton search = new JButton("Look up");
    private final JButton add = new JButton("Add to portfolio");
    private final JLabel security = new JLabel();
    private final JLabel quote = new JLabel();
    private final JLabel metrics = new JLabel();
    private final JLabel lookupStatus = new JLabel();
    private final JLabel portfolioStatus = new JLabel(" ");
    private final JTextArea holdings = new JTextArea(4, 40);
    private final StockPriceChart chart = new StockPriceChart();

    private StockLookupState currentResult;
    private boolean busy;

    public StockLookupView(StockLookupController lookupController, StockLookupViewModel lookupModel,
                           AddTransactionController transactionController, DashboardViewModel dashboardModel,
                           String username) {
        super(new BorderLayout(0, 12));
        this.lookupController = lookupController;
        this.transactionController = transactionController;
        this.username = username;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(buildSearchPanel(), BorderLayout.NORTH);
        add(chart, BorderLayout.CENTER);
        add(buildPortfolioPanel(), BorderLayout.SOUTH);

        search.addActionListener(event -> lookUp());
        ticker.addActionListener(event -> lookUp());
        add.addActionListener(event -> addToPortfolio());
        lookupModel.addPropertyChangeListener(event -> {
            StockLookupState state = (StockLookupState) event.getNewValue();
            SwingUtilities.invokeLater(() -> showLookup(state));
        });
        dashboardModel.addPropertyChangeListener(event -> {
            DashboardState state = (DashboardState) event.getNewValue();
            SwingUtilities.invokeLater(() -> showPortfolio(state));
        });
        showLookup(lookupModel.getState());
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        JLabel tickerLabel = new JLabel("Ticker");
        tickerLabel.setLabelFor(ticker);
        controls.add(tickerLabel);
        controls.add(ticker);
        controls.add(search);
        panel.add(controls);
        panel.add(security);
        panel.add(quote);
        panel.add(metrics);
        panel.add(lookupStatus);
        return panel;
    }

    private JPanel buildPortfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        controls.add(add);
        controls.add(portfolioStatus);
        holdings.setEditable(false);
        holdings.getAccessibleContext().setAccessibleName("Portfolio holdings");
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(holdings), BorderLayout.CENTER);
        return panel;
    }

    private void lookUp() {
        if (busy) {
            return;
        }
        String submittedTicker = ticker.getText();
        runTask(() -> lookupController.execute(submittedTicker), lookupStatus, "Loading market data...");
    }

    private void showLookup(StockLookupState state) {
        currentResult = state;
        if (state.hasResult()) {
            String heading = state.getTicker().equals(state.getName())
                    ? state.getTicker() : state.getTicker() + " - " + state.getName();
            security.setText(heading);
            quote.setText("Latest close: " + state.getQuote() + " (" + state.getTradingDay() + ")");
            metrics.setText(state.getMetrics());
        } else {
            security.setText(" ");
            quote.setText(" ");
            metrics.setText(" ");
        }
        chart.setHistory(state.getCloses(), state.getFirstDate(), state.getLastDate());
        lookupStatus.setText(state.getStatus().isEmpty() ? " " : state.getStatus());
        updateButtons();
    }

    private void showPortfolio(DashboardState state) {
        if (!state.getError().isEmpty()) {
            portfolioStatus.setText(state.getError());
            return;
        }
        portfolioStatus.setText("Purchase recorded. Portfolio value: " + state.getTotalValue());
        String rows = String.join(System.lineSeparator(), state.getHoldingRows());
        if (!state.getWarning().isEmpty()) {
            rows += System.lineSeparator() + state.getWarning();
        }
        holdings.setText(rows);
        holdings.setCaretPosition(0);
    }

    private void addToPortfolio() {
        StockLookupState selected = currentResult;
        JTextField quantityField = new JTextField("1");
        JTextField priceField = new JTextField(Double.toString(selected.getPrice()));
        JTextField dateField = new JTextField(LocalDate.now().toString());
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        addFormField(form, "Shares", quantityField);
        addFormField(form, "Price per share", priceField);
        addFormField(form, "Trade date (YYYY-MM-DD)", dateField);

        while (JOptionPane.showConfirmDialog(this, form, "Record a " + selected.getTicker() + " buy",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                double quantity = Double.parseDouble(quantityField.getText().strip());
                double price = Double.parseDouble(priceField.getText().strip());
                LocalDate date = LocalDate.parse(dateField.getText().strip());
                if (!Double.isFinite(quantity) || quantity <= 0 || !Double.isFinite(price) || price <= 0) {
                    throw new NumberFormatException("A positive, finite trade value is required.");
                }
                runTask(() -> transactionController.execute(username, selected.getTicker(), "BUY",
                        quantity, price, date), portfolioStatus, "Recording purchase...");
                return;
            } catch (NumberFormatException | DateTimeParseException failure) {
                JOptionPane.showMessageDialog(this, "Enter positive numbers and a date in YYYY-MM-DD format.",
                        "Check trade details", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addFormField(JPanel form, String label, JTextField field) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setLabelFor(field);
        form.add(fieldLabel);
        form.add(field);
    }

    private void runTask(Runnable task, JLabel status, String loadingMessage) {
        busy = true;
        status.setText(loadingMessage);
        updateButtons();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    status.setText("The operation was interrupted.");
                } catch (ExecutionException failure) {
                    status.setText("The operation could not be completed.");
                } finally {
                    busy = false;
                    updateButtons();
                }
            }
        }.execute();
    }

    private void updateButtons() {
        ticker.setEnabled(!busy);
        search.setEnabled(!busy);
        add.setEnabled(!busy && currentResult.hasResult());
    }
}
