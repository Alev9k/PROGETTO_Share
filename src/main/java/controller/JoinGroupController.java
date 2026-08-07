package controller;

import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.User;

/** Controller del caso d'uso di iscrizione a un gruppo. */
public class JoinGroupController {

    private final GroupDAO groupDAO;
    private final UserDAO userDAO;

    public JoinGroupController(GroupDAO groupDAO, UserDAO userDAO) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
    }

    /**
     * L'utente delega il comportamento alla propria specializzazione.
     * Un Operator si iscrive, mentre le altre tipologie rifiutano l'operazione.
     */
    public void joinGroup(String username, int groupId, String groupName) throws Exception {
        Group targetGroup = groupDAO.findGroupById(groupId);
        if (targetGroup == null) {
            throw new Exception("Nessun gruppo trovato con questo ID.");
        }
        if (groupName == null || !targetGroup.getName().equalsIgnoreCase(groupName.trim())) {
            throw new Exception("Accesso negato: le credenziali del gruppo non corrispondono.");
        }

        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new Exception("Utente non trovato.");
        }

        try {
            user.joinGroup(targetGroup);
        } catch (IllegalStateException e) {
            throw new Exception(e.getMessage());
        }

        groupDAO.update(targetGroup);
        userDAO.updateUser(user);
    }
}
