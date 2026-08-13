package model.dao;

import model.factory.UserFactory;
import model.entity.*;
import exceptions.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUserDAO implements UserDAO {
    private static final String FILE_NAME = "users.csv";

    @Override
    public void save(User user) throws DAOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writeUserToFile(out, user);
        } catch (IOException e) {
            // Invece di stampare, lanciamo l'eccezione specifica
            throw new DAOException("Impossibile scrivere sul file " + FILE_NAME);
        }
    }

    @Override
    public List<User> findAll() throws DAOException {
        List<User> users = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Riga utente non valida.");
                }
                users.add(UserFactory.createUser(
                        Integer.parseInt(parts[2]), parts[0], parts[1]));
            }
        } catch (IOException | RuntimeException e) {
            throw new DAOException("Errore critico nella lettura del database CSV.");
        }
        return users;
    }

    @Override
    public void updateUser(User updatedUser) throws DAOException, UserNotFoundException {
        List<User> users = findAll(); // Può lanciare DAOException
        boolean found = false;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new UserNotFoundException("Utente '" + updatedUser.getUsername() + "' non trovato.");
        }

        rewriteFile(users);
    }

    private void rewriteFile(List<User> users) throws DAOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, false))) {
            for (User u : users) {
                writeUserToFile(out, u);
            }
        } catch (IOException e) {
            throw new DAOException("Errore durante la riscrittura del file VFS.");
        }
    }

    private void writeUserToFile(PrintWriter out, User user) {
        int type = switch (user) {
            case Admin ignored -> 1;
            case Operator ignored -> 2;
            default -> throw new IllegalArgumentException("Tipo utente non supportato.");
        };
        out.println(user.getUsername() + "," + user.getPassword() + "," + type);
    }

    @Override
    public User findByUsername(String username) throws DAOException {
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
