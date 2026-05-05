package controller;

import exceptions.ItemInUseException;
import model.bean.GroupBean;
import model.bean.ItemBean;
import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.*;
import exceptions.DAOException;
import exceptions.DuplicateItemNameException;
import java.util.ArrayList;
import java.util.List;

public class ManageItemsController {
    private final GroupDAO groupDAO;
    private final UserDAO userDAO;

    public ManageItemsController(GroupDAO groupDAO, UserDAO userDAO) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
    }

    public List<ItemBean> getItemList(int groupID) throws Exception {
        Group group = groupDAO.findGroupById(groupID);
        List<ItemBean> beanList = new ArrayList<>();

        for (Item item : group.getItems()) {
            beanList.add(new ItemBean(item.getName(), item.getAssetName())); // assetName da gestire se necessario
        }
        return beanList;
    }

    // Step 11a: Verifica unicità nome[cite: 1]
    public void addNewItem(ItemBean bean, int groupID)
            throws DAOException, DuplicateItemNameException {

        Group group = groupDAO.findGroupById(groupID);

        for (Item existing : group.getItems()) {
            if (existing.getName().equalsIgnoreCase(bean.getItemName())) {
                // Passo 11ab: Errore nome duplicato[cite: 1]
                throw new DuplicateItemNameException("Nome item già esistente nel gruppo.");
            }
        }

        Item newItem = new Item(bean.getItemName(), bean.getAssetName());
        group.addItem(newItem); // Step 12a[cite: 1]
        groupDAO.update(group);
    }

    public void removeItem(ItemBean itemBean, GroupBean groupBean)
            throws ItemInUseException {

        Group group = groupDAO.findGroupById(groupBean.getGroupId());
        Item item = group.getSingleItem(itemBean.getItemName());

        // Step 9: Verifica se l'Item è in uso[cite: 1]
        if (item.checkActiveness()) {
            throw new ItemInUseException("Impossibile eliminare: l'oggetto è attualmente in uso.");
        }

        List<Booking> bookingsToRemove = item.getBookings();

        for (Booking b : bookingsToRemove) {
            // Per ogni prenotazione, individuiamo l'operatore tramite lo username nel booking
            Operator op = (Operator) userDAO.findByUsername(b.getOperatorName());
            if (op != null) {
                // Rimuoviamo la prenotazione dalla lista dell'operatore[cite: 1]
                op.removeBookingByItem(item.getName(), group.getGroupID());
                // Aggiorniamo l'utente sul database/file[cite: 1]
                userDAO.updateUser(op);

                // Step 11: Qui andrebbe inserita la logica di notifica all'operatore[cite: 1]
                // notifyOperatorOfCancellation(op, item.getName());
            }
        }

        // 4. Rimuoviamo l'Item dal gruppo[cite: 1]
        group.removeItem(item);

        // 5. Persistenza: Salviamo lo stato aggiornato del gruppo[cite: 1]
        groupDAO.update(group);
    }

}
