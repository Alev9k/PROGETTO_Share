package boundary.javafx;

import boundary.javafx.navigation.SceneNavigator;
import controller.AccessNotificationController;
import controller.EventNotificationController;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import model.bean.MembershipRequestBean;
import model.bean.NotificationBean;
import model.session.SessionContext;

import java.util.List;

public class OperatorDashboardGraphicController {
    @FXML private Label welcomeLabel;
    @FXML private Label notificationLabel;

    private SceneNavigator navigator;
    private AccessNotificationController notificationController;
    private EventNotificationController eventNotificationController;

    /** Inizializza la dashboard con l'operatore autenticato. */
    public void initData(AccessNotificationController notificationController,
                         EventNotificationController eventNotificationController,
                         SceneNavigator navigator,
                         SessionContext session) {
        this.notificationController = notificationController;
        this.eventNotificationController = eventNotificationController;
        this.navigator = navigator;
        String username = session.requireCurrentUser().getUsername();
        this.welcomeLabel.setText("Bentornato, " + username + "!");
        Platform.runLater(this::showNotifications);
    }

    private void showNotifications() {
        showAccessNotifications();
        showEventNotifications();
    }

    @FXML
    private void handleMyGroups(Event event) {
        navigator.showMyGroups();
    }

    @FXML
    private void handleJoinGroup(Event event) {
        navigator.showRequestGroupAccess();
    }

    @FXML
    private void handleBookings(Event event) {
        navigator.showMyBookings();
    }

    @FXML
    private void handleLogout(Event event) {
        navigator.logout();
    }

    private void showAccessNotifications() {
        try {
            List<MembershipRequestBean> notifications =
                    notificationController.getUnreadResults();

            if (notifications.isEmpty()) {
                notificationLabel.setText("Non hai nuove notifiche sulle richieste di accesso.");
                return;
            }

            notificationLabel.setText(notifications.size() == 1
                    ? "Hai una nuova notifica di accesso."
                    : "Hai " + notifications.size() + " nuove notifiche di accesso.");

            StringBuilder message = new StringBuilder();
            for (MembershipRequestBean notification : notifications) {
                message.append(notification.getGroupName())
                        .append(": ")
                        .append(notification.getStatusLabel())
                        .append('\n');
                notificationController.markAsRead(notification);
            }
            showAlert(Alert.AlertType.INFORMATION, "Esito richieste di accesso",
                    message.toString().trim());
        } catch (Exception e) {
            notificationLabel.setText("Impossibile caricare le notifiche.");
        }
    }

    private void showEventNotifications() {
        try {
            List<NotificationBean> notifications =
                    eventNotificationController.getUnread();
            if (notifications.isEmpty()) {
                return;
            }

            notificationLabel.setText(notifications.size() == 1
                    ? "Hai una nuova notifica sulle prenotazioni."
                    : "Hai " + notifications.size() + " nuove notifiche sulle prenotazioni.");
            StringBuilder message = new StringBuilder();
            for (NotificationBean notification : notifications) {
                message.append(notification.getTypeLabel()).append(" - ")
                        .append(notification.getCreatedAtLabel()).append('\n')
                        .append(notification.getMessage()).append("\n\n");
                eventNotificationController.markAsRead(notification.getNotificationId());
            }
            showAlert(Alert.AlertType.INFORMATION, "Aggiornamento prenotazioni",
                    message.toString().trim());
        } catch (Exception e) {
            notificationLabel.setText("Impossibile caricare le notifiche.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
