package exceptions;

/** Segnala che esiste già un gruppo con il nome richiesto. */
public class DuplicateGroupNameException extends Exception {
    public DuplicateGroupNameException(String message) {
        super(message);
    }
}
