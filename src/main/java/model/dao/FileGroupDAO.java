package model.dao;

import model.entity.Group;
import exceptions.DAOException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileGroupDAO implements GroupDAO {
    private static final String fileName = "groups.csv";

    @Override
    public List<Group> findAll() throws DAOException {
        List<Group> groups = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) return groups; // Lista vuota, triggera 1a nel controller[cite: 1]

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    groups.add(new Group(Integer.parseInt(parts[0]), parts[1]));
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore di lettura VFS: impossibile caricare i gruppi.");
        }
        return groups;
    }

    @Override
    public void save(Group group) throws DAOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, true))) {
            out.println(group.getGroupID() + "," + group.getName());
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

        // Riscriviamo il file aggiornato
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, false))) {
            for (Group g : allGroups) {
                out.println(g.getGroupID() + "," + g.getName());
            }
        } catch (IOException e) {
            throw new DAOException("Errore di aggiornamento VFS.");
        }
    }

    @Override
    public Group findGroupById(int id) throws DAOException {
        // 1. Recuperiamo tutti i gruppi leggendo il file CSV
        List<Group> allGroups = findAll();

        // 2. Cerchiamo quello con l'ID corretto
        for (Group g : allGroups) {
            if (g.getGroupID() == id) {
                return g; // Trovato!
            }
        }

        return null; // Non trovato
    }

    @Override
    public void delete(int groupID) throws DAOException {
        List<Group> allGroups = findAll();
        // Rimuoviamo il gruppo con l'ID corrispondente
        allGroups.removeIf(g -> g.getGroupID() == groupID);

        // Riscriviamo il file senza il gruppo eliminato
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, false))) {
            for (Group g : allGroups) {
                out.println(g.getGroupID() + "," + g.getName());
            }
        } catch (IOException e) {
            throw new DAOException("Errore durante l'eliminazione del gruppo dal File System.");
        }
    }
}