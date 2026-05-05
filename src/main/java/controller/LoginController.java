package controller;

import exceptions.InvalidCredentialsException;
import model.bean.Role;
import model.bean.UserBean;
import model.dao.*;
import model.entity.*;

public class LoginController {
    private UserDAO userDao;

    public LoginController(UserDAO userDao) {
        this.userDao = userDao;
    }

    public UserBean login(String name, String pass) throws InvalidCredentialsException {
        User u = userDao.findByUsername(name);
        if (u == null || !u.getPassword().equals(pass)) {
            throw new InvalidCredentialsException();
        }
        return switch (u) {
            case Admin admin -> new UserBean(u.getUsername(), Role.ADMIN);
            case Operator operator -> new UserBean(u.getUsername(), Role.OPERATOR);
            case Technician technician -> new UserBean(u.getUsername(), Role.TECHNICIAN);
            default -> throw new IllegalStateException("Tipo utente non previsto: " + u.getClass().getName());
        };
    }
}