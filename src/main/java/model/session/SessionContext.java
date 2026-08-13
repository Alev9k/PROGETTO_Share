package model.session;

import model.bean.UserBean;

/** Espone in sola lettura l'identità autenticata ai controller applicativi. */
@FunctionalInterface
public interface SessionContext {
    UserBean requireCurrentUser();
}
