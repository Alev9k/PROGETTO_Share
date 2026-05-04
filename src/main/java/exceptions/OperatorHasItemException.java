package exceptions;

// Lanciata se l'operatore ha un Item e non può essere bloccato (Step 9 Operatori)
public class OperatorHasItemException extends Exception {
    public OperatorHasItemException(String msg) {
        super(msg);
    }
}
