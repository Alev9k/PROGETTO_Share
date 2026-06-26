package controller;
/*
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
            beanList.add(new ItemBean(item.getName(), item.getAssetName()));
        }
        return beanList;
    }

    public void addNewItem(ItemBean bean) throws DAOException, DuplicateItemNameException {
        // Step 11a: Verifica unicità nome
        for (Item existing : contextGroup.getItems()) {
            if (existing.getName().equalsIgnoreCase(bean.getItemName())) {
                throw new DuplicateItemNameException("Nome item già esistente nel gruppo.");
            }
        }

        Item newItem = new Item(bean.getItemName(), bean.getAssetName());
        contextGroup.addItem(newItem); // Step 12a
        groupDAO.update(contextGroup);
    }

    public void removeItem(ItemBean itemBean) throws Exception {
        Item item = contextGroup.getSingleItem(itemBean.getItemName());

        // Step 9: Verifica se l'Item è in uso
        if (item.checkActiveness()) {
            throw new ItemInUseException("Impossibile eliminare: l'oggetto è attualmente in uso.");
        }

        // Step 10: Pulizia prenotazioni
        List<Booking> bookingsToRemove = item.getBookings();
        for (Booking b : bookingsToRemove) {
            Operator op = (Operator) userDAO.findByUsername(b.getOperatorName());
            if (op != null) {
                op.removeBookingByItem(item.getName(), contextGroup.getGroupID());
                userDAO.updateUser(op);
            }
        }

        contextGroup.removeItem(item);
        groupDAO.update(contextGroup);
    }
}
*/