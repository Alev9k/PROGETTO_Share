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
    private final Group contextGroup; // Il nostro attributo final per il contesto

    public ManageOperatorsController(int groupID, UserDAO userDAO, GroupDAO groupDAO) throws DAOException {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        // Recuperiamo il gruppo una sola volta all'inizio
        this.contextGroup = groupDAO.findGroupById(groupID);

        if (this.contextGroup == null) {
            throw new DAOException("Gruppo non trovato per l'ID: " + groupID);
        }
    }

    public List<OperatorBean> getOperatorList() {
        List<OperatorBean> beanList = new ArrayList<>();
        // Usiamo l'attributo contextGroup invece di cercarlo nel DAO
        for (Operator op : contextGroup.getOperators()) {
            int status = op.checkActiveness(contextGroup.getGroupID()) ? 0 : 1;
            beanList.add(new OperatorBean(op.getUsername(), status));
        }
        return beanList;
    }

    public void toggleBlock(OperatorBean opBean) throws DAOException, OperatorHasItemException {
        Operator op = (Operator) userDAO.findByUsername(opBean.getUsername());
        int gID = contextGroup.getGroupID();

        // Step 9: Verifica possesso Item
        if (op.checkActiveness(gID) && op.hasItemFromGroup(gID)) {
            throw new OperatorHasItemException("L'operatore possiede un bene e non può essere bloccato.");
        }

        op.toggleState(gID); // Step 10

        if (!op.checkActiveness(gID)) {
            List<Booking> removed = op.cancelGroupBookings(gID);
            for (Booking b : removed) {
                Item item = contextGroup.getSingleItem(b.getItemName());
                if (item != null) {
                    item.removeBooking(b);
                }
            }
        }

        // Persistenza
        userDAO.updateUser(op);
        groupDAO.update(contextGroup); // Salviamo le modifiche al gruppo
    }
}