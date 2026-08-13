package model.factory;

import model.entity.*;

public class UserFactory {
    private UserFactory() {
        // La factory espone esclusivamente operazioni statiche.
    }

    public static User createUser(int type, String username, String password) {
        return switch (type) {
            case 1 -> new Admin(username, password);
            case 2 -> new Operator(username, password);
            default -> throw new IllegalArgumentException("Tipo utente non supportato");
        };
    }
}
