package controller;

import exceptions.ItemInUseException;
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
    private final Group contextGroup;

    public ManageItemsController(int groupID, GroupDAO groupDAO, UserDAO userDAO) throws DAOException {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.contextGroup = groupDAO.findGroupById(groupID);

        if (this.contextGroup == null) {
            throw new DAOException("Gruppo non trovato.");
        }
    }

    public List<ItemBean> getItemList() {
        List<ItemBean> beanList = new ArrayList<>();
        for (Item item : contextGroup.getItems()) {
            beanList.add(new ItemBean(item.getName(), item.getPriority(), item.getMaxUsageTime()));
        }
        return beanList;
    }

    public void addNewItem(ItemBean bean) throws DAOException, DuplicateItemNameException {
        if (bean == null || bean.getItemName() == null || bean.getItemName().isBlank()) {
            throw new IllegalArgumentException("Il nome dell'item non pu\u00f2 essere vuoto.");
        }
        if (bean.getPriority() < 1 || bean.getPriority() > 5) {
            throw new IllegalArgumentException("La priorit\u00e0 deve essere compresa tra 1 e 5.");
        }
        if (bean.getMaxUsageTime() <= 0) {
            throw new IllegalArgumentException("Il tempo massimo di utilizzo deve essere positivo.");
        }

        // Step 11a: Verifica unicità nome
        for (Item existing : contextGroup.getItems()) {
            if (existing.getName().equalsIgnoreCase(bean.getItemName())) {
                throw new DuplicateItemNameException("Nome item già esistente nel gruppo.");
            }
        }

        int nextItemId = contextGroup.getItems().stream()
                .mapToInt(Item::getItemID)
                .max()
                .orElse(0) + 1;
        Item newItem = new Item(nextItemId, bean.getItemName().trim(), contextGroup.getGroupID(),
                bean.getPriority(), bean.getMaxUsageTime());
        contextGroup.addItem(newItem); // Step 12a
        groupDAO.update(contextGroup);
    }

    public void removeItem(ItemBean itemBean) throws Exception {
        Item item = contextGroup.getSingleItem(itemBean.getItemName());
        if (item == null) {
            throw new IllegalArgumentException("L'item selezionato non esiste nel gruppo.");
        }

        // Step 9: Verifica se l'Item è in uso
        if (item.checkActiveness()) {
            throw new ItemInUseException("Impossibile eliminare: l'oggetto è attualmente in uso.");
        }

        // Step 10: Pulizia prenotazioni
        List<Booking> bookingsToRemove = new ArrayList<>(item.getBookings());
        for (Booking b : bookingsToRemove) {
            Operator op = contextGroup.findOperatorByUsername(b.getOperatorName());
            if (op != null) {
                op.removeBookingByItem(item.getName(), contextGroup.getGroupID());
                userDAO.updateUser(op);
            }
        }

        contextGroup.removeItem(item);
        groupDAO.update(contextGroup);
    }
}
