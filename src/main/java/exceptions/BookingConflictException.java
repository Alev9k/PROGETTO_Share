package exceptions;

public class BookingConflictException extends IllegalStateException {
    public BookingConflictException(String message) {
        super(message);
    }
}
