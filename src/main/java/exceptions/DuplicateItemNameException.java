package exceptions;

// Lanciata se il nome dell'Item esiste già nel gruppo (Step 11ab Items)
public class DuplicateItemNameException extends Exception {
    public DuplicateItemNameException(String msg) {
        super(msg);
    }
}
