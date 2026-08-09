package controller;

import model.bean.*;
import model.entity.*;
import model.dao.*;
import exceptions.*;

import java.util.ArrayList;
import java.util.List;
/**
 * Controller per la gestione dei gruppi.
 * Implementa la logica di business definita nel diagramma VOPC e nei casi d'uso.
 */
public class ManageGroupController {
    private final UserDAO userDAO; // Per recuperare Admin/Operator
    private final GroupDAO groupDAO; // Per gestire i gruppi (Demo o VFS)

    public ManageGroupController(UserDAO uDao, GroupDAO gDao) {
        this.userDAO = uDao;
        this.groupDAO = gDao;
    }


    // --- Metodi di Recupero Dati ---

    public List<GroupBean> getGroupList(String adminUsername){
        User admin = userDAO.findByUsername(adminUsername);
        List<GroupBean> beanList = new ArrayList<>();

        if (admin == null || !admin.canManageGroups()) {
            throw new IllegalArgumentException("Utente non autorizzato alla gestione dei gruppi.");
        }

        List<Group> managedGroups = groupDAO.findGroupsByOwnerUsername(adminUsername);
        if (managedGroups.isEmpty()) {
            // Compatibilità con gruppi creati prima dell'introduzione del proprietario persistente.
            managedGroups = admin.getManagedGroups();
        }

        for (Group g : managedGroups) {
            beanList.add(new GroupBean(g.getName(),g.getGroupID()));
        }
        return beanList;
    }

    /**
     * Elimina un gruppo, sganciandolo dagli operatori e dall'amministratore.
     *
     * @param groupBean Il bean del gruppo da eliminare
     * @param adminBean Il bean dell'amministratore che possiede il gruppo
     */
    public void deleteGroup(GroupBean groupBean, UserBean adminBean) throws Exception {
        int groupID = groupBean.getGroupId();

        // 1. Recuperiamo il gruppo completo
        Group group = groupDAO.findGroupById(groupID);
        if (group == null) {
            throw new Exception("Il gruppo selezionato non esiste più nel sistema.");
        }

        // 2. Controllo sicurezza: ci sono item in uso?
        for (Item item : group.getItems()) {
            if (item.checkActiveness()) {
                throw new Exception("Impossibile eliminare il gruppo: il bene '" +
                        item.getName() + "' è attualmente in uso.");
            }
        }

        // 3. Pulizia Operatori (Sganciamo il gruppo dagli operatori)
        for (Operator op : group.getOperators()) {
            op.cancelGroupBookings(groupID);
            op.removeState(groupID);
            userDAO.updateUser(op);
        }

        // 4. NOVITÀ: Pulizia Admin (Sganciamo il gruppo dall'Admin)
        User admin = userDAO.findByUsername(adminBean.getUsername());
        if (admin == null || !admin.canManageGroups()) {
            throw new IllegalArgumentException("Utente non autorizzato alla gestione dei gruppi.");
        }
        admin.removeManagedGroup(groupID);
        userDAO.updateUser(admin);

        // 5. Eliminazione definitiva dal file groups.csv
        groupDAO.delete(groupID);
    }
}
