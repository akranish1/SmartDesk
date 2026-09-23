package smartdesk.booking.exception;

public class DeskUnavailableException extends RuntimeException {

    public DeskUnavailableException(String message) {
        super(message);
    }
}