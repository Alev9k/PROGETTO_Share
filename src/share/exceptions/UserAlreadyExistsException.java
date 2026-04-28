package share.exceptions;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String username) {
        super("L'utente con username '" + username + "' è già presente nel sistema.");
    }
}