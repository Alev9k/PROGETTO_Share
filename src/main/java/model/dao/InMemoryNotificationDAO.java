package model.dao;

import exceptions.DAOException;
import model.entity.Notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class InMemoryNotificationDAO implements NotificationDAO {
    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public synchronized void saveAll(Collection<Notification> newNotifications) {
        Objects.requireNonNull(newNotifications);
        Set<String> newIds = new HashSet<>();
        for (Notification notification : newNotifications) {
            Objects.requireNonNull(notification);
            if (!newIds.add(notification.getNotificationId())
                    || findById(notification.getNotificationId()) != null) {
                throw new DAOException("Esiste già una notifica con questo identificativo.");
            }
        }
        notifications.addAll(newNotifications);
    }

    @Override
    public synchronized void update(Notification updatedNotification) {
        Objects.requireNonNull(updatedNotification);
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getNotificationId()
                    .equals(updatedNotification.getNotificationId())) {
                notifications.set(i, updatedNotification);
                return;
            }
        }
        throw new DAOException("Notifica non trovata per l'aggiornamento.");
    }

    @Override
    public synchronized List<Notification> findAll() {
        return new ArrayList<>(notifications);
    }
}
