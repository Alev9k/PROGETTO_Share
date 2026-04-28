package controller;

import exceptions.InvalidCredentialsException;
import model.dao.*;
import model.entity.User;

public class LoginController {
    private UserDAO userDao;

    public LoginController(UserDAO userDao) {
        this.userDao = userDao;
    }

    public User login(String name, String pass) throws InvalidCredentialsException {
        User u = userDao.findByUsername(name);
        if (u == null || !u.getPassword().equals(pass)) {
            throw new InvalidCredentialsException();
        }
        return u;
    }
}