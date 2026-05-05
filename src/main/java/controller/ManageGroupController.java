package controller;

import model.bean.*;
import model.entity.*;
import model.dao.*;
import exceptions.*;

import java.util.ArrayList;
import java.util.List;
/**
 * Controller per la gestione dei gruppi, dei beni (Items) e dei membri (Operators).
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
        Admin admin = (Admin) userDAO.findByUsername(adminUsername);
        List<GroupBean> beanList = new ArrayList<>();

        for (Group g : admin.getGroups()) {
            beanList.add(new GroupBean(g.getName(),g.getGroupID()));
        }
        return beanList;
    }

    public void deleteGroup(int groupID) throws Exception {
        // 1. Recuperiamo il gruppo completo
        Group group = groupDAO.findGroupById(groupID);

        // 2. Controllo sicurezza: ci sono item in uso?
        for (Item item : group.getItems()) {
            if (item.checkActiveness()) { // Stato 1: In Uso
                throw new Exception("Impossibile eliminare il gruppo: il bene '" +
                        item.getName() + "' è attualmente in uso.");
            }
        }

        // 3. Pulizia Operatori: dobbiamo rimuovere lo stato e i booking di questo gruppo da ogni utente
        List<Operator> allOperators = group.getOperators();
        for (Operator op : allOperators) {
            op.cancelGroupBookings(groupID);
            op.removeState(groupID);
            // NOTA: Va aggiunta un sistema di notifica di eliminazione del gruppo agli operator
            userDAO.updateUser(op);
        }

        // 4. Eliminazione definitiva
        groupDAO.delete(groupID);
    }
}