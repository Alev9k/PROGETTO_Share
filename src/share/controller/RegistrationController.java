package share.controller;

import share.model.entity.User;
import share.model.factory.UserFactory;
import share.model.dao.*;

public class RegistrationController {
    private UserDAO userDao;

    public RegistrationController(UserDAO userDao) {
        this.userDao = userDao;
    }

    public boolean register(String name, String pass, int type) {
        if (userDao.findByUsername(name) != null) return false;

        User newUser = UserFactory.createUser(type, name, pass);
        userDao.save(newUser);
        return true;
    }
}