package controller;

import model.bean.GroupBean;
import model.dao.GroupDAO;
import model.entity.Group;
import model.entity.User;
import model.session.SessionContext;

import java.util.ArrayList;
import java.util.List;
/**
 * Controller per la gestione dei gruppi.
 * Implementa la logica di business definita nel diagramma VOPC e nei casi d'uso.
 */
public class ManageGroupController {
    private final GroupDAO groupDAO; // Per gestire i gruppi (Demo o VFS)
    private final SessionContext session;

    public ManageGroupController(GroupDAO gDao, SessionContext session) {
        this.groupDAO = gDao;
        this.session = session;
    }


    // --- Metodi di Recupero Dati ---

    public List<GroupBean> getGroupList(){
        User admin = session.requireCurrentUser();
        String adminUsername = admin.getUsername();
        List<GroupBean> beanList = new ArrayList<>();

        if (!admin.canManageGroups()) {
            throw new IllegalArgumentException("Utente non autorizzato alla gestione dei gruppi.");
        }

        List<Group> managedGroups = groupDAO.findGroupsByOwnerUsername(adminUsername);
        if (managedGroups.isEmpty()) {
            // Compatibilità con gruppi creati prima dell'introduzione del proprietario persistente.
            managedGroups = admin.getManagedGroups();
        }

        for (Group g : managedGroups) {
            beanList.add(new GroupBean(g.getGroupID(), g.getName(),
                    g.getOpenTime(), g.getCloseTime(),
                    g.getAccessToken(), g.getOwnerUsername()));
        }
        return beanList;
    }
}
