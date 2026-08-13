package model.session;

import model.entity.User;

import java.util.Objects;

/**
 * Sessione dell'applicazione desktop. Il processo può avere un solo utente
 * autenticato alla volta.
 */
public final class UserSession implements SessionContext {
    private User currentUser;

    private UserSession() {
    }

    private static class Holder {
        private static final UserSession INSTANCE = new UserSession();
    }

    public static UserSession getInstance() {
        return Holder.INSTANCE;
    }

    public void open(User user) {
        if (currentUser != null) {
            throw new IllegalStateException("Esiste già una sessione attiva.");
        }
        currentUser = Objects.requireNonNull(user, "L'utente autenticato è obbligatorio.");
    }

    @Override
    public User requireCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Nessun utente autenticato.");
        }
        return currentUser;
    }

    public void close() {
        currentUser = null;
    }
}
