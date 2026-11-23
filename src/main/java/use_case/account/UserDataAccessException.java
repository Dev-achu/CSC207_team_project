package use_case.account;

public class UserDataAccessException extends RuntimeException {
    public UserDataAccessException(String message) {
        super(message);
    }
}
