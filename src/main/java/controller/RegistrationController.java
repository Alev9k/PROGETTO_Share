package controller;

import exceptions.UserAlreadyExistsException;
import model.entity.User;
import model.factory.UserFactory;
import model.dao.*;

public class RegistrationController {
    private UserDAO userDao;

    public RegistrationController(UserDAO userDao) {
        this.userDao = userDao;
    }

    public void register(String name, String pass, int type) throws UserAlreadyExistsException {
        if (userDao.findByUsername(name) != null) {
            throw new UserAlreadyExistsException(name); // Logica d'errore propria
        }
        User newUser = UserFactory.createUser(type, name, pass);
        userDao.save(newUser);
    }
}