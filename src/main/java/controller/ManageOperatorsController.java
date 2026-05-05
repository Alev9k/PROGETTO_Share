package controller;

import model.bean.OperatorBean;
import model.dao.*;
import model.entity.*;
import exceptions.DAOException;
import exceptions.OperatorHasItemException;
import java.util.ArrayList;
import java.util.List;

public class ManageOperatorsController {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;

    public ManageOperatorsController(UserDAO userDAO, GroupDAO groupDAO) {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
    }

    public List<OperatorBean> getOperatorList(int groupID) throws Exception {
        Group group = groupDAO.findGroupById(groupID);
        List<OperatorBean> beanList = new ArrayList<>();

        for (Operator op : group.getOperators()) {
            // Otteniamo lo stato specifico per questo gruppo[cite: 1]
            int status = op.checkActiveness(groupID) ? 0 : 1;
            beanList.add(new OperatorBean(op.getUsername(), status));
        }
        return beanList;
    }

    // Step 10: Aggiorna lo stato e notifica
    public void toggleBlock(OperatorBean opBean, int groupID)
            throws DAOException, OperatorHasItemException {

        Operator op = (Operator) userDAO.findByUsername(opBean.getUsername());
        Group group = groupDAO.findGroupById(groupID);

        // Step 9: Verifica se possiede un Item nel gruppo
        if (op.checkActiveness(groupID) && op.hasItemFromGroup(groupID)) {
            // Passo 9c: Errore specifico
            throw new OperatorHasItemException("L'operatore possiede un bene e non può essere bloccato.");
        }

        op.toggleState(groupID); // Step 10[cite: 1]
        if (!op.checkActiveness(groupID)) {
            // Rimuoviamo lato Operatore e otteniamo la lista di cosa è stato tolto
            List<Booking> removed = op.cancelGroupBookings(groupID);

            // Per ogni prenotazione rimossa, dobbiamo informare l'Item corrispondente
            for (Booking b : removed) {
                // Usiamo il metodo getSingleItem che abbiamo creato prima nel Gruppo
                Item item = group.getSingleItem(b.getItemName());
                if (item != null) {
                    item.removeBooking(b); // Rimuoviamo lato Item
                }
            }
        }

        // 4. PERSISTENZA: Salviamo le modifiche su file (VFS) o memoria (Demo)
        userDAO.updateUser(op);     // Salva lo stato dell'operatore
        groupDAO.update(group);     // Salva lo stato del gruppo (che contiene gli Item aggiornati)
    }
}