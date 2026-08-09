package model.dao;

import model.entity.Group;
import exceptions.DAOException;
import java.io.*;
import java.time.LocalTime; // Scommenta se usi LocalTime nel costruttore
import java.util.ArrayList;
import java.util.List;

public class FileGroupDAO implements GroupDAO {
    private static final String fileName = "groups.csv";

    @Override
    public List<Group> findAll() throws DAOException {
        List<Group> groups = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) return groups;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // Formato corrente: ID, Nome, OpenTime, CloseTime, Token, Proprietario.
                // Le righe storiche a 4 campi restano leggibili.
                if (parts.length == 4 || parts.length == 6) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String openTime = parts[2];
                    String closeTime = parts[3];
                    String accessToken = parts.length == 6 ? parts[4] : "";
                    String ownerUsername = parts.length == 6 ? parts[5] : "";

                    groups.add(new Group(id, name, LocalTime.parse(openTime),
                            LocalTime.parse(closeTime), accessToken, ownerUsername));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DAOException("Errore di lettura VFS: impossibile caricare i gruppi.");
        }
        return groups;
    }

    @Override
    public void save(Group group) throws DAOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, true))) {
            writeGroup(out, group);
        } catch (IOException e) {
            throw new DAOException("Errore di scrittura VFS: impossibile salvare il gruppo.");
        }
    }

    @Override
    public void update(Group group) throws DAOException {
        List<Group> allGroups = findAll();

        for (int i = 0; i < allGroups.size(); i++) {
            if (allGroups.get(i).getGroupID() == group.getGroupID()) {
                allGroups.set(i, group);
                break;
            }
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, false))) {
            for (Group g : allGroups) {
                writeGroup(out, g);
            }
        } catch (IOException e) {
            throw new DAOException("Errore di aggiornamento VFS.");
        }
    }

    @Override
    public Group findGroupById(int id) throws DAOException {
        List<Group> allGroups = findAll();
        for (Group g : allGroups) {
            if (g.getGroupID() == id) {
                return g;
            }
        }
        return null;
    }

    @Override
    public void delete(int groupID) throws DAOException {
        List<Group> allGroups = findAll();
        allGroups.removeIf(g -> g.getGroupID() == groupID);

        // Riscriviamo il file senza il gruppo eliminato (sempre con 4 campi)
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, false))) {
            for (Group g : allGroups) {
                writeGroup(out, g);
            }
        } catch (IOException e) {
            throw new DAOException("Errore durante l'eliminazione del gruppo dal File System.");
        }
    }

    private void writeGroup(PrintWriter out, Group group) {
        out.println(group.getGroupID() + "," +
                group.getName() + "," +
                group.getOpenTime() + "," +
                group.getCloseTime() + "," +
                group.getAccessToken() + "," +
                group.getOwnerUsername());
    }
}
