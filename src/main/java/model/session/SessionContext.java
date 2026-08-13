package model.session;

import model.entity.User;

/** Espone in sola lettura l'identità autenticata ai controller applicativi. */
@FunctionalInterface
public interface SessionContext {
    User requireCurrentUser();
}
