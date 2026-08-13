package controller;

import exceptions.DAOException;
import exceptions.DuplicateItemNameException;
import exceptions.UnauthorizedOperationException;
import model.bean.CreateItemBean;
import model.bean.ItemBean;
import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.Item;
import model.entity.User;
import model.session.SessionContext;

import java.util.List;

/** Controller del caso d'uso di visualizzazione e creazione degli item. */
public class ManageItemsController {
    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final Group contextGroup;
    private final SessionContext session;

    public ManageItemsController(int groupID, GroupDAO groupDAO, UserDAO userDAO,
                                 SessionContext session) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.session = session;
        this.contextGroup = groupDAO.findGroupById(groupID);

        if (this.contextGroup == null) {
            throw new DAOException("Gruppo non trovato.");
        }
    }

    public List<ItemBean> getItemList()
            throws UnauthorizedOperationException {
        requireAuthorizedAdmin();
        return contextGroup.getItems().stream()
                .map(this::toBean)
                .toList();
    }

    public ItemBean createItem(CreateItemBean bean)
            throws DuplicateItemNameException, UnauthorizedOperationException {
        requireAuthorizedAdmin();
        validate(bean);

        int nextItemId = contextGroup.getItems().stream()
                .mapToInt(Item::getItemID)
                .max()
                .orElse(0) + 1;
        Item newItem = new Item(nextItemId, bean.getItemName().trim(),
                contextGroup.getGroupID(), bean.getPriority(), bean.getMaxUsageTime());

        contextGroup.addItem(newItem);
        groupDAO.update(contextGroup);
        return toBean(newItem);
    }

    private void validate(CreateItemBean bean) {
        if (bean == null || bean.getItemName() == null || bean.getItemName().isBlank()) {
            throw new IllegalArgumentException("Il nome dell'item non può essere vuoto.");
        }
        if (bean.getItemName().indexOf('\n') >= 0 || bean.getItemName().indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Il nome dell'item deve occupare una sola riga.");
        }
        if (bean.getPriority() < 1 || bean.getPriority() > 5) {
            throw new IllegalArgumentException("La priorità deve essere compresa tra 1 e 5.");
        }
        if (bean.getMaxUsageTime() < 30 || bean.getMaxUsageTime() % 30 != 0) {
            throw new IllegalArgumentException(
                    "Il tempo massimo di utilizzo deve essere un multiplo di 30 minuti.");
        }
    }

    private void requireAuthorizedAdmin()
            throws UnauthorizedOperationException {
        User admin = userDAO.findByUsername(session.requireCurrentUser().getUsername());
        if (admin == null || !admin.canManageGroups()) {
            throw new UnauthorizedOperationException("Amministratore non autorizzato.");
        }

        boolean ownsGroup = contextGroup.isManagedBy(admin.getUsername())
                || admin.getManagedGroups().stream()
                .anyMatch(group -> group.getGroupID() == contextGroup.getGroupID());
        if (!ownsGroup) {
            throw new UnauthorizedOperationException(
                    "Non puoi gestire gli item di questo gruppo.");
        }
    }

    private ItemBean toBean(Item item) {
        return new ItemBean(item.getItemID(), item.getName(), item.getPriority(),
                item.getMaxUsageTime(), item.getStatus());
    }
}
