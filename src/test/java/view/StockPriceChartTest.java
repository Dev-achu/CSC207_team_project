package view;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockPriceChartTest {
    @Test
    void chartPlotsAFlatSeriesAndClearsItWhenHistoryIsEmpty() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            StockPriceChart chart = new StockPriceChart();

            chart.setHistory(List.of(150.0, 150.0), "2026-01-05", "2026-01-06");
            assertTrue(plottedPixels(render(chart)) > 100, "A flat series should draw a line across the chart.");
            chart.setHistory(List.of(), "", "");
            assertEquals(0, plottedPixels(render(chart)));
        });
    }

    private BufferedImage render(StockPriceChart chart) {
        int width = 660;
        int height = 260;
        chart.setSize(width, height);
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            chart.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return canvas;
    }

    private int plottedPixels(BufferedImage canvas) {
        int plotted = 0;
        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                Color pixel = new Color(canvas.getRGB(x, y));
                if (pixel.getBlue() - pixel.getRed() > 70) {
                    plotted++;
                }
            }
        }
        return plotted;
    }
}
