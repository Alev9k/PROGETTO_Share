package controller;

import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.User;
import model.bean.GroupBean;
import model.bean.UserBean;
import model.factory.AccessTokenGenerator;

import java.time.LocalTime;
import java.util.List;

public class CreateGroupController {

    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final AccessTokenGenerator tokenGenerator;

    public CreateGroupController(GroupDAO groupDAO, UserDAO userDAO) {
        this(groupDAO, userDAO, new AccessTokenGenerator());
    }

    CreateGroupController(GroupDAO groupDAO, UserDAO userDAO, AccessTokenGenerator tokenGenerator) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.tokenGenerator = tokenGenerator;
    }

    /**
     * Crea un nuovo gruppo ricevendo i dati esclusivamente tramite Bean.
     *
     * @param groupBean Il bean contenente i dati inseriti nella view.
     * @param adminBean Il bean dell'utente loggato.
     */
    public GroupBean createGroup(GroupBean groupBean, UserBean adminBean) throws Exception {

        // 1. Estrazione dai Bean
        String groupName = groupBean.getGroupName();
        LocalTime openTime = groupBean.getOpenTime();
        LocalTime closeTime = groupBean.getCloseTime();
        String adminUsername = adminBean.getUsername();

        // 2. Validazione base e logica di business
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del gruppo non può essere vuoto.");
        }
        if (openTime == null || closeTime == null) {
            throw new IllegalArgumentException("Gli orari di apertura e chiusura sono obbligatori.");
        }
        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("L'orario di apertura deve precedere l'orario di chiusura.");
        }

        // 3. Controllo duplicati e calcolo del nuovo ID
        List<Group> existingGroups = groupDAO.findAll();
        int newId = 1;

        for (Group g : existingGroups) {
            if (g.getName().equalsIgnoreCase(groupName.trim())) {
                throw new Exception("Esiste già un gruppo chiamato '" + groupName + "'. Scegli un nome diverso.");
            }
            if (g.getGroupID() >= newId) {
                newId = g.getGroupID() + 1;
            }
        }

        User admin = userDAO.findByUsername(adminUsername);
        if (admin == null || !admin.canManageGroups()) {
            throw new Exception("Errore critico: Amministratore '" + adminUsername + "' non trovato.");
        }

        // 4. Creazione dell'entità reale e salvataggio
        String accessToken;
        do {
            accessToken = tokenGenerator.generate();
        } while (groupDAO.findGroupByAccessToken(accessToken) != null);

        Group newGroup = new Group(newId, groupName.trim(), openTime, closeTime,
                accessToken, adminUsername);
        groupDAO.save(newGroup);

        // 5. Associazione all'Admin
        admin.addManagedGroup(newGroup);
        userDAO.updateUser(admin);

        return new GroupBean(newGroup.getGroupID(), newGroup.getName(),
                newGroup.getOpenTime(), newGroup.getCloseTime(),
                newGroup.getAccessToken(), newGroup.getOwnerUsername());
    }
}
