package controller;

import exceptions.InvalidCredentialsException;
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
        UserBean authenticatedUser = new UserBean(u.getUsername(), u.getRole());
        session.open(u);
        return authenticatedUser;
    }
}
