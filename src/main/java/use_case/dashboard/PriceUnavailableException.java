package use_case.dashboard;

/**
 * Thrown by a data access object when a current price cannot be fetched,
 * for example because of a network failure or an API rate limit.
 */
public class PriceUnavailableException extends Exception {

    public PriceUnavailableException(String message) {
        super(message);
    }
}
