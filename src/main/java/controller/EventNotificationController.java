package controller;

import model.bean.NotificationBean;
import model.dao.NotificationDAO;
import model.dao.UserDAO;
import model.entity.Notification;
import model.entity.User;
import model.session.SessionContext;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Controller BCE per la consultazione delle notifiche persistenti di dominio. */
public class EventNotificationController {
    private final UserDAO userDAO;
    private final NotificationDAO notificationDAO;
    private final SessionContext session;

    public EventNotificationController(UserDAO userDAO, NotificationDAO notificationDAO,
                                       SessionContext session) {
        this.userDAO = Objects.requireNonNull(userDAO);
        this.notificationDAO = Objects.requireNonNull(notificationDAO);
        this.session = Objects.requireNonNull(session);
    }

    public List<NotificationBean> getUnread() {
        User user = requireUser();
        return notificationDAO.findUnreadByRecipient(user.getUsername()).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt))
                .map(this::toBean)
                .toList();
    }

    public void markAsRead(String notificationId) {
        User user = requireUser();
        Notification notification = notificationDAO.findById(notificationId);
        if (notification == null
                || !notification.getRecipientUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("Notifica non trovata.");
        }
        notification.markAsRead();
        notificationDAO.update(notification);
    }

    private User requireUser() {
        User user = userDAO.findByUsername(session.requireCurrentUser().getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Utente non valido.");
        }
        return user;
    }

    private NotificationBean toBean(Notification notification) {
        return new NotificationBean(notification.getNotificationId(),
                notification.getType().getLabel(), notification.getMessage(),
                notification.getCreatedAt());
    }
}
