package use_case.stock_lookup;

import entity.AssetClass;
import entity.PricePoint;
import entity.Security;
import entity.StockMetrics;
import entity.StockSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

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
        new StockLookupInteractor(this::marketData, presenter).execute(new StockLookupInputData(" aapl "));

        assertEquals("AAPL", presenter.successData.getTicker());
        assertEquals(180.5, presenter.successData.getQuote().getClose());
        assertNull(presenter.errorMessage);
    }

    @Test
    void tickerWithDotIsPreserved() {
        RecordingPresenter presenter = new RecordingPresenter();
        new StockLookupInteractor(this::marketData, presenter).execute(new StockLookupInputData("brk.b"));

        assertEquals("BRK.B", presenter.successData.getTicker());
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ", ""})
    void blankTickerReportsError(String ticker) {
        RecordingPresenter presenter = new RecordingPresenter();
        new StockLookupInteractor(this::unexpectedLookup, presenter).execute(new StockLookupInputData(ticker));

        assertEquals("Please enter a stock ticker.", presenter.errorMessage);
        assertNull(presenter.successData);
    }

    @Test
    void missingInputReportsError() {
        RecordingPresenter presenter = new RecordingPresenter();
        new StockLookupInteractor(this::unexpectedLookup, presenter).execute(null);

        assertEquals("Please enter a stock ticker.", presenter.errorMessage);
        assertNull(presenter.successData);
    }

    @ParameterizedTest
    @ValueSource(strings = {"AA PL", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"})
    void malformedTickerDoesNotCallDataAccess(String ticker) {
        RecordingPresenter presenter = new RecordingPresenter();
        new StockLookupInteractor(this::unexpectedLookup, presenter).execute(new StockLookupInputData(ticker));

        assertEquals("Enter a valid ticker, such as AAPL or BRK.B.", presenter.errorMessage);
        assertNull(presenter.successData);
    }

    @Test
    void dataAccessFailureIsPresented() {
        RecordingPresenter presenter = new RecordingPresenter();
        StockLookupDataAccessInterface dataAccess = ticker -> {
            throw new StockDataException("Ticker not found: INVALID.");
        };
        new StockLookupInteractor(dataAccess, presenter).execute(new StockLookupInputData("invalid"));

        assertEquals("Ticker not found: INVALID.", presenter.errorMessage);
        assertNull(presenter.successData);
    }

    @Test
    void cachedSnapshotIncludesNotice() {
        RecordingPresenter presenter = new RecordingPresenter();
        StockLookupDataAccessInterface dataAccess = ticker ->
                new StockLookupDataAccessInterface.Result(marketData(ticker).getSnapshot(), "Provider unavailable.");
        new StockLookupInteractor(dataAccess, presenter).execute(new StockLookupInputData("AAPL"));

        assertEquals("Showing cached data. Provider unavailable.", presenter.successData.getDataNotice());
    }

    private StockLookupDataAccessInterface.Result marketData(String ticker) {
        Security security = new Security(ticker, ticker, AssetClass.STOCK);
        PricePoint quote = new PricePoint(security, LocalDate.of(2026, 1, 5), 180.5);
        StockSnapshot snapshot = new StockSnapshot(quote, List.of(quote), new StockMetrics(null, null, null));
        return new StockLookupDataAccessInterface.Result(snapshot, "");
    }

    private StockLookupDataAccessInterface.Result unexpectedLookup(String ticker) {
        return fail("Input validation should stop the data access call.");
    }
}
