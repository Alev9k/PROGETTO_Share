package share.controller;

import share.model.dao.*;
import share.model.entity.User;

public class LoginController {
    private UserDAO userDao;

    public LoginController(UserDAO userDao) {
        this.userDao = userDao;
    }

    public User login(String name, String pass) {
        User u = userDao.findByUsername(name);
        if (u != null && u.getPassword().equals(pass)) {
            return u;
        }
        return null;
    }
}