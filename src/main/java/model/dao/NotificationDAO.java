package model.dao;

import model.entity.Notification;

import java.util.Collection;
import java.util.List;

public interface NotificationDAO {
    void saveAll(Collection<Notification> notifications);
    void update(Notification notification);
    List<Notification> findAll();

    default void save(Notification notification) {
        saveAll(List.of(notification));
    }

    default Notification findById(String notificationId) {
        return findAll().stream()
                .filter(notification -> notification.getNotificationId().equals(notificationId))
                .findFirst()
                .orElse(null);
    }

    default List<Notification> findUnreadByRecipient(String username) {
        return findAll().stream()
                .filter(notification -> notification.getRecipientUsername().equals(username))
                .filter(notification -> !notification.isRead())
                .toList();
    }
}
