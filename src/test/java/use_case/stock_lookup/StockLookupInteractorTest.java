package use_case.stock_lookup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StockLookupInteractorTest {
    private static class RecordingPresenter implements StockLookupOutputBoundary {
        StockLookupOutputData successData;
        String errorMessage;

        @Override
        public void prepareSuccessView(StockLookupOutputData outputData) {
            successData = outputData;
        }

        @Override
        public void prepareFailView(String message) {
            errorMessage = message;
        }
    }

    @Test
    void lowercaseTickerIsNormalized() {
        RecordingPresenter presenter = new RecordingPresenter();
        new StockLookupInteractor(presenter).execute(new StockLookupInputData(" aapl "));

        assertEquals("AAPL", presenter.successData.getTicker());
        assertNull(presenter.errorMessage);
    }

    @Test
    void tickerWithDotIsPreserved() {
        RecordingPresenter presenter = new RecordingPresenter();
        new StockLookupInteractor(presenter).execute(new StockLookupInputData("brk.b"));

        assertEquals("BRK.B", presenter.successData.getTicker());
    }

    @Test
    void blankTickerReportsError() {
        RecordingPresenter presenter = new RecordingPresenter();
        new StockLookupInteractor(presenter).execute(new StockLookupInputData("  "));

        assertEquals("Please enter a stock ticker.", presenter.errorMessage);
        assertNull(presenter.successData);
    }

    @Test
    void missingInputReportsError() {
        RecordingPresenter presenter = new RecordingPresenter();
        new StockLookupInteractor(presenter).execute(null);

        assertEquals("Please enter a stock ticker.", presenter.errorMessage);
        assertNull(presenter.successData);
    }
}
