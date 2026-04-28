package model.factory;

import share.model.entity.*;

public class UserFactory {
    public static User createUser(int type, String username, String password) {
        return switch (type) {
            case 1 -> new Admin(username, password);
            case 2 -> new Operator(username, password);
            case 3 -> new Technician(username, password);
            default -> throw new IllegalArgumentException("Tipo utente non supportato");
        };
    }
}
