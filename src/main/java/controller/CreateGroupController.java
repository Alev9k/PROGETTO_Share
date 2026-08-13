package controller;

import exceptions.DuplicateGroupNameException;
import exceptions.UnauthorizedOperationException;
import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.User;
import model.bean.GroupBean;
import model.factory.AccessTokenGenerator;
import model.session.SessionContext;

import java.time.LocalTime;
import java.util.List;

public class CreateGroupController {

    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final AccessTokenGenerator tokenGenerator;
    private final SessionContext session;

    public CreateGroupController(GroupDAO groupDAO, UserDAO userDAO,
                                 SessionContext session) {
        this(groupDAO, userDAO, session, new AccessTokenGenerator());
    }

    CreateGroupController(GroupDAO groupDAO, UserDAO userDAO, SessionContext session,
                          AccessTokenGenerator tokenGenerator) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.tokenGenerator = tokenGenerator;
        this.session = session;
    }

    /**
     * Crea un nuovo gruppo ricevendo i dati esclusivamente tramite Bean.
     *
     * @param groupBean Il bean contenente i dati inseriti nella view.
     */
    public GroupBean createGroup(GroupBean groupBean)
            throws DuplicateGroupNameException, UnauthorizedOperationException {

        // 1. Estrazione dai Bean
        String groupName = groupBean.getGroupName();
        LocalTime openTime = groupBean.getOpenTime();
        LocalTime closeTime = groupBean.getCloseTime();
        User admin = session.requireCurrentUser();
        String adminUsername = admin.getUsername();

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
                throw new DuplicateGroupNameException(
                        "Esiste già un gruppo chiamato '" + groupName
                                + "'. Scegli un nome diverso.");
            }
            if (g.getGroupID() >= newId) {
                newId = g.getGroupID() + 1;
            }
        }

        if (!admin.canManageGroups()) {
            throw new UnauthorizedOperationException(
                    "L'utente corrente non è autorizzato a creare gruppi.");
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
