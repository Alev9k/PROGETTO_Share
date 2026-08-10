package exceptions;

/** Segnala che l'utente corrente non può eseguire l'operazione richiesta. */
public class UnauthorizedOperationException extends Exception {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
