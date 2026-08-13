package model.session;

import model.bean.UserBean;

import java.util.Objects;

/**
 * Sessione dell'applicazione desktop. Il processo può avere un solo utente
 * autenticato alla volta.
 */
public class UserSession implements SessionContext {
    private UserBean currentUser;

    private UserSession() {
    }

    private static class Holder {
        private static final UserSession INSTANCE = new UserSession();
    }

    public static UserSession getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void open(UserBean user) {
        if (currentUser != null) {
            throw new IllegalStateException("Esiste già una sessione attiva.");
        }
        currentUser = Objects.requireNonNull(user, "L'utente autenticato è obbligatorio.");
    }

    @Override
    public synchronized UserBean requireCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Nessun utente autenticato.");
        }
        return currentUser;
    }

    public synchronized boolean isActive() {
        return currentUser != null;
    }

    public synchronized void close() {
        currentUser = null;
    }
}
