package model.dao;

import exceptions.DAOException;
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

    @Override
    public void delete(int groupID) throws DAOException {
        // Utilizziamo removeIf: una funzione molto potente di Java che scorre la lista
        // e rimuove l'elemento se la condizione (l'ID coincide) è vera.
        boolean rimosso = groups.removeIf(g -> g.getGroupID() == groupID);

        // Nonostante la tua osservazione corretta (l'ID viene da una lista sicura),
        // è buona pratica di programmazione avere un feedback interno.
        if (!rimosso) {
            // In memoria non lanciamo eccezioni critiche, ma potremmo loggare l'evento
            System.out.println("Avviso: Nessun gruppo trovato con ID " + groupID + " durante l'eliminazione.");
        }
    }
}