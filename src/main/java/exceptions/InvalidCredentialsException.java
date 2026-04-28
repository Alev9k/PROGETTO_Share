package exceptions;

public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException() {
        super("Username o password non corretti. Riprova.");
    }
}