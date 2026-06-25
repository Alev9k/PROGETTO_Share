package controller;

import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.Admin;
import model.entity.Group;
import exceptions.DAOException;

import java.time.LocalTime;
import java.util.List;

/**
 * Controller applicativo per la creazione di un nuovo gruppo.
 * Gestisce la logica di business e le interazioni con i DAO necessari.
 */
public class CreateGroupController {

    private final GroupDAO groupDAO;
    private final UserDAO userDAO;

    public CreateGroupController(GroupDAO groupDAO, UserDAO userDAO) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
    }

    /**
     * Crea un nuovo gruppo e lo associa all'amministratore.
     *
     * @param groupName Il nome del nuovo gruppo.
     * @param adminUsername L'username dell'amministratore che sta creando il gruppo.
     * @param openTime Orario di apertura del gruppo.
     * @param closeTime Orario di chiusura del gruppo.
     * @throws Exception Se i dati non sono validi o se ci sono errori di salvataggio.
     */
    public void createGroup(String groupName, String adminUsername, LocalTime openTime, LocalTime closeTime) throws Exception {

        // 1. Validazione base e logica di business sugli orari
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del gruppo non può essere vuoto.");
        }

        if (openTime == null || closeTime == null) {
            throw new IllegalArgumentException("Gli orari di apertura e chiusura sono obbligatori.");
        }

        // Verifica logica: l'apertura deve venire prima della chiusura
        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("L'orario di apertura deve essere precedente all'orario di chiusura.");
        }

        // 2. Controllo duplicati e calcolo del nuovo ID
        List<Group> existingGroups = groupDAO.findAll();
        int newId = 1;

        for (Group g : existingGroups) {
            // Verifica che il nome non sia già in uso nel sistema
            if (g.getName().equalsIgnoreCase(groupName.trim())) {
                throw new Exception("Esiste già un gruppo chiamato '" + groupName + "'. Scegli un nome diverso.");
            }
            // Calcolo ID incrementale
            if (g.getGroupID() >= newId) {
                newId = g.getGroupID() + 1;
            }
        }

        // 3. Creazione e salvataggio della nuova entità Group
        Group newGroup = new Group(newId, groupName.trim(), openTime, closeTime);
        groupDAO.save(newGroup);

        // 4. Associazione del gruppo all'Admin
        Admin admin = (Admin) userDAO.findByUsername(adminUsername);
        if (admin != null) {
            // Aggiungiamo il gruppo alla lista dell'admin
            admin.getGroups().add(newGroup);

            // Aggiorniamo l'admin nel database/file system
            userDAO.updateUser(admin);
        } else {
            throw new Exception("Errore critico: Amministratore '" + adminUsername + "' non trovato.");
        }
    }
}