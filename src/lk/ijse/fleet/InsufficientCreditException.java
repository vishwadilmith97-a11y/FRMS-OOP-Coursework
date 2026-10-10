package lk.ijse.fleet;

public class InsufficientCreditException extends Exception {
    private static final long serialVersionUID = 1L;

    public InsufficientCreditException(String message) {
        super(message);
    }
}
