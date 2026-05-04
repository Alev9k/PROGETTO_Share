package exceptions;

// Lanciata se si tenta di eliminare un Item attualmente in uso (Step 9 Items)
public class ItemInUseException extends Exception {
    public ItemInUseException(String msg) { super(msg); }
}

