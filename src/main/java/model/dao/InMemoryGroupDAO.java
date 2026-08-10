package model.dao;

import model.entity.Group;
import java.util.ArrayList;
import java.util.List;

public class InMemoryGroupDAO implements GroupDAO {
    private final List<Group> groups = new ArrayList<>();

    @Override
    public List<Group> findAll() {
        return new ArrayList<>(groups);
    }

    @Override
    public void save(Group group) {
        groups.add(group);
    }

    @Override
    public void update(Group group) {
        // In memoria l'oggetto è già aggiornato tramite i riferimenti
        // ma potremmo cercare e sostituire per sicurezza
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getGroupID() == group.getGroupID()) {
                groups.set(i, group);
                break;
            }
        }
    }

    public Group findGroupById(int id) {
        // Cerchiamo nella lista 'groups' l'elemento che ha il groupID uguale a quello cercato
        return groups.stream()
                .filter(g -> g.getGroupID() == id)
                .findFirst()
                .orElse(null); // Se non esiste, restituiamo null
    }
}
