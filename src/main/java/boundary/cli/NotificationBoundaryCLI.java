package boundary.cli;

import controller.AccessNotificationController;
import controller.EventNotificationController;
import model.bean.MembershipRequestBean;
import model.bean.NotificationBean;

import java.util.List;

/** Mostra nelle dashboard CLI le notifiche destinate all'attore autenticato. */
public class NotificationBoundaryCLI {
    private final AccessNotificationController accessController;
    private final EventNotificationController eventController;

    public NotificationBoundaryCLI(AccessNotificationController accessController,
                                   EventNotificationController eventController) {
        this.accessController = accessController;
        this.eventController = eventController;
    }

    public void showForAdmin() {
        try {
            long pending = accessController.countPendingForAdmin();
            System.out.println(pending == 1
                    ? "Richieste di accesso pendenti: 1"
                    : "Richieste di accesso pendenti: " + pending);
            showPersistentNotifications("Nuove segnalazioni sugli item");
        } catch (RuntimeException e) {
            System.err.println("Impossibile caricare le notifiche: " + e.getMessage());
        }
    }

    public void showForOperator() {
        try {
            List<MembershipRequestBean> accessResults = accessController.getUnreadResults();
            if (!accessResults.isEmpty()) {
                System.out.println("\n--- ESITO RICHIESTE DI ACCESSO ---");
                for (MembershipRequestBean result : accessResults) {
                    System.out.println(result.getGroupName() + ": " + result.getStatusLabel());
                    accessController.markAsRead(result);
                }
            }
            showPersistentNotifications("Aggiornamenti sulle prenotazioni");
        } catch (RuntimeException e) {
            System.err.println("Impossibile caricare le notifiche: " + e.getMessage());
        }
    }

    private void showPersistentNotifications(String title) {
        List<NotificationBean> notifications = eventController.getUnread();
        if (notifications.isEmpty()) {
            return;
        }
        System.out.println("\n--- " + title.toUpperCase() + " ---");
        for (NotificationBean notification : notifications) {
            System.out.println(notification.getTypeLabel() + " - "
                    + notification.getCreatedAtLabel());
            System.out.println(notification.getMessage());
            eventController.markAsRead(notification.getNotificationId());
        }
    }
}
