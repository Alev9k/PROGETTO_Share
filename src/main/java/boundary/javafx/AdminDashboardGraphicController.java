package boundary.javafx;

import boundary.javafx.navigation.NavigationException;
import boundary.javafx.navigation.SceneNavigator;
import controller.AccessNotificationController;
import controller.EventNotificationController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.event.Event;
import model.bean.NotificationBean;
import model.session.SessionContext;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDashboardGraphicController {

    private static final Logger LOGGER = Logger.getLogger(
            AdminDashboardGraphicController.class.getName());

    @FXML private Label welcomeLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label eventNotificationsLabel;

    private SceneNavigator navigator;

    /**
     * Inizializza la Dashboard.
     */
    public void initData(AccessNotificationController notificationController,
                         EventNotificationController eventNotificationController,
                         SceneNavigator navigator, SessionContext session) {
        this.navigator = navigator;
        String username = session.requireCurrentUser().getUsername();

        // CORREZIONE 1: Testo di benvenuto
        this.welcomeLabel.setText("Bentornato, " + username + "!");
        try {
            long pendingCount = notificationController.countPendingForAdmin();
            this.pendingRequestsLabel.setText(pendingCount == 1
                    ? "Hai 1 richiesta di accesso in attesa."
                    : "Hai " + pendingCount + " richieste di accesso in attesa.");
        } catch (Exception e) {
            this.pendingRequestsLabel.setText("Impossibile caricare le notifiche.");
        }
        Platform.runLater(() -> showEventNotifications(eventNotificationController));
    }

    private void showEventNotifications(EventNotificationController controller) {
        try {
            List<NotificationBean> notifications = controller.getUnread();
            if (notifications.isEmpty()) {
                eventNotificationsLabel.setText("Non hai nuove segnalazioni sugli item.");
                return;
            }
            eventNotificationsLabel.setText(notifications.size() == 1
                    ? "Hai una nuova segnalazione su un item."
                    : "Hai " + notifications.size() + " nuove segnalazioni sugli item.");
            StringBuilder message = new StringBuilder();
            for (NotificationBean notification : notifications) {
                message.append(notification.getTypeLabel()).append(" - ")
                        .append(notification.getCreatedAtLabel()).append('\n')
                        .append(notification.getMessage()).append("\n\n");
                controller.markAsRead(notification.getNotificationId());
            }
            Alert alert = new Alert(Alert.AlertType.WARNING, message.toString().trim());
            alert.setTitle("Segnalazioni item");
            alert.setHeaderText("Nuove notifiche");
            alert.showAndWait();
        } catch (Exception e) {
            eventNotificationsLabel.setText("Impossibile caricare le segnalazioni.");
        }
    }

    // CORREZIONE 2: Usiamo "Event" invece di "ActionEvent" così
    // funziona sia con i Button (sidebar) che con i VBox (cards)
    @FXML
    private void handleManageGroups(Event event) {
        try {
            navigator.showManageGroups();
        } catch (NavigationException e) {
            LOGGER.log(Level.SEVERE, "Navigazione verso la gestione gruppi fallita.", e);
            showError("Errore", "Impossibile caricare la gestione gruppi.");
        }
    }

    @FXML
    private void handleCreateGroup(Event event) {
        try {
            navigator.showCreateGroup();
        } catch (NavigationException e) {
            LOGGER.log(Level.SEVERE, "Navigazione verso la creazione del gruppo fallita.", e);
            showError("Errore", "Impossibile caricare la creazione del gruppo.");
        }
    }

    @FXML
    private void handleLogout(Event event) {
        try {
            navigator.logout();
        } catch (Exception e) {
            showError("Errore", "Impossibile tornare al login.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
