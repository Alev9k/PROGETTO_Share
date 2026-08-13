package controller;

import exceptions.InvalidCredentialsException;
import model.bean.Role;
import model.bean.UserBean;
import model.dao.*;
import model.entity.*;
import model.session.UserSession;

public class LoginController {
    private final UserDAO userDao;
    private final UserSession session;

    public LoginController(UserDAO userDao, UserSession session) {
        this.userDao = userDao;
        this.session = session;
    }

    public UserBean login(String name, String pass) throws InvalidCredentialsException {
        User u = userDao.findByUsername(name);
        if (u == null || !u.getPassword().equals(pass)) {
            throw new InvalidCredentialsException();
        }
        UserBean authenticatedUser = switch (u) {
            case Admin admin -> new UserBean(u.getUsername(), Role.ADMIN);
            case Operator operator -> new UserBean(u.getUsername(), Role.OPERATOR);
            default -> throw new IllegalStateException("Tipo utente non previsto: " + u.getClass().getName());
        };
        session.open(authenticatedUser);
        return authenticatedUser;
    }
}
