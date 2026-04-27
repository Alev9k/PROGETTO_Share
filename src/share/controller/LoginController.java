package share.controller;

import share.model.entity.*;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    // Simulazione database in memoria
    private static List<User> userDatabase = new ArrayList<>();

    public boolean register(String name, String pass, int type) {
        for (User u : userDatabase) {
            if (u.getUsername().equalsIgnoreCase(name)) return false;
        }

        User newUser;
        switch (type) {
            case 1 -> newUser = new Admin(name, pass);
            case 2 -> newUser = new Operator(name, pass);
            default -> newUser = new Technician(name, pass); // Assumendo classe Technician esistente
        }

        userDatabase.add(newUser);
        return true;
    }

    public User login(String name, String pass) {
        for (User u : userDatabase) {
            if (u.getUsername().equals(name) && u.getPassword().equals(pass)) {
                return u; // Ritorna l'oggetto specifico (Admin, Operator, ecc.)
            }
        }
        return null;
    }
}