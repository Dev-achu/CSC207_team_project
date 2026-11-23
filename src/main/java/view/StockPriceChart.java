package view;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Draws daily closing prices in trading-session order. */
final class StockPriceChart extends JPanel {
    private List<Double> closes = List.of();
    private String firstDate = "";
    private String lastDate = "";

    StockPriceChart() {
        setPreferredSize(new Dimension(660, 260));
        setBackground(Color.WHITE);
        getAccessibleContext().setAccessibleName("Daily closing price chart");
    }

    void setHistory(List<Double> closes, String firstDate, String lastDate) {
        this.closes = closes;
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (closes.isEmpty() || getWidth() < 160 || getHeight() < 110) {
            return;
        }

        Graphics2D chart = (Graphics2D) graphics.create();
        try {
            chart.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double low = Collections.min(closes);
            double high = Collections.max(closes);
            if (high == low) {
                double padding = Math.max(0.01, high * 0.01);
                low -= padding;
                high += padding;
            }

            int left = 78;
            int top = 34;
            int right = getWidth() - 24;
            int bottom = getHeight() - 32;
            drawAxes(chart, left, top, right, bottom, low, high);
            drawPrices(chart, left, top, right, bottom, low, high);
        } finally {
            chart.dispose();
        }
    }

    private void drawAxes(Graphics2D chart, int left, int top, int right, int bottom, double low, double high) {
        chart.setColor(Color.DARK_GRAY);
        chart.drawString("Daily close", left, 18);
        chart.drawString(firstDate, left, bottom + 20);
        if (!lastDate.equals(firstDate)) {
            chart.drawString(lastDate, right - chart.getFontMetrics().stringWidth(lastDate), bottom + 20);
        }
        for (int line = 0; line <= 4; line++) {
            int y = top + (bottom - top) * line / 4;
            double value = high - (high - low) * line / 4;
            chart.setColor(new Color(222, 228, 236));
            chart.drawLine(left, y, right, y);
            chart.setColor(Color.DARK_GRAY);
            chart.drawString(String.format(Locale.ROOT, "%,.2f", value), 4, y + 4);
        }
    }

    private void drawPrices(Graphics2D chart, int left, int top, int right, int bottom, double low, double high) {
        chart.setColor(new Color(16, 91, 153));
        int previousX = left;
        int previousY = bottom;
        int steps = Math.max(1, closes.size() - 1);
        for (int index = 0; index < closes.size(); index++) {
            int x = left + (int) Math.round((right - left) * ((double) index / steps));
            double fraction = (closes.get(index) - low) / (high - low);
            int y = bottom - (int) Math.round(fraction * (bottom - top));
            if (index > 0) {
                chart.drawLine(previousX, previousY, x, y);
            } else {
                chart.fillOval(x - 2, y - 2, 5, 5);
            }
            previousX = x;
            previousY = y;
        }
    }
}
