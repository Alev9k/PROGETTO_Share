package share.model.dao;

import share.model.factory.UserFactory;
import share.model.entity.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUserDAO implements UserDAO {
    private final String fileName = "users.csv";

    @Override
    public void save(User user) {
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, true))) {
            // Determiniamo il "tag" numerico per la generalizzazione
            int type = (user instanceof Admin) ? 1 : (user instanceof Operator) ? 2 : 3;
            out.println(user.getUsername() + "," + user.getPassword() + "," + type);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio su file.");
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    // Usiamo la Factory per creare l'oggetto specifico corretto
                    User user = UserFactory.createUser(Integer.parseInt(parts[2]), parts[0], parts[1]);
                    users.add(user);
                }
            }
        } catch (FileNotFoundException e) {
            // File non ancora creato, restituiamo lista vuota
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public User findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}