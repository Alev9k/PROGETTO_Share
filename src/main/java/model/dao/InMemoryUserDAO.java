package model.dao;

import exceptions.UserNotFoundException;
import model.entity.User;
import java.util.ArrayList;
import java.util.List;

public class InMemoryUserDAO implements UserDAO {
    private static final List<User> userList = new ArrayList<>();

    @Override
    public void save(User user) {
        userList.add(user);
    }

    @Override
    public User findByUsername(String username) {
        return userList.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userList);
    }

    @Override
    public void updateUser(User updatedUser) throws UserNotFoundException {
        boolean found = false;

        // 1. Cerchiamo l'utente all'interno della nostra lista
        for (int i = 0; i < userList.size(); i++) {
            User existingUser = userList.get(i);

            // Confrontiamo gli username (identificativo univoco)
            if (existingUser.getUsername().equals(updatedUser.getUsername())) {
                // 2. Sostituiamo il vecchio oggetto con quello aggiornato
                userList.set(i, updatedUser);
                found = true;
                break; // Usciamo dal ciclo una volta trovato
            }
        }

        // 3. Se il ciclo finisce senza aver trovato nulla, segnaliamo l'errore
        if (!found) {
            throw new UserNotFoundException("Impossibile aggiornare: l'utente "
                    + updatedUser.getUsername() + " non esiste in memoria.");
        }
    }
}
