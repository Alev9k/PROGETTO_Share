package controller.observer;

import model.dao.GroupDAO;
import model.dao.NotificationDAO;
import model.entity.Group;
import model.entity.Notification;
import model.entity.NotificationType;
import model.observer.ItemBrokenEvent;
import model.observer.ItemObserver;

import java.util.Objects;
import java.util.UUID;

/** Crea la notifica persistente destinata al proprietario del gruppo. */
public class AdminBrokenItemNotificationObserver implements ItemObserver {
    private final GroupDAO groupDAO;
    private final NotificationDAO notificationDAO;

    public AdminBrokenItemNotificationObserver(GroupDAO groupDAO,
                                               NotificationDAO notificationDAO) {
        this.groupDAO = Objects.requireNonNull(groupDAO);
        this.notificationDAO = Objects.requireNonNull(notificationDAO);
    }

    @Override
    public void onItemBroken(ItemBrokenEvent event) {
        Group group = groupDAO.findGroupById(event.groupId());
        if (group == null || group.getOwnerUsername().isBlank()) {
            throw new IllegalStateException(
                    "Non è possibile individuare l'Admin responsabile del gruppo.");
        }
        String message = "L'item '" + event.itemName() + "' del gruppo '"
                + group.getName() + "' è stato segnalato guasto dall'operatore "
                + event.reportingOperator() + ".";
        notificationDAO.save(new Notification(UUID.randomUUID().toString(),
                group.getOwnerUsername(), NotificationType.ITEM_BROKEN,
                message, event.reportedAt()));
    }
}
