package boundary.javafx;

import controller.AccessNotificationController;
import controller.ControllerFactory;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import model.bean.MembershipRequestBean;
import model.bean.Role;
import model.bean.UserBean;

import java.util.List;

public class OperatorDashboardGraphicController {
    @FXML private Label welcomeLabel;
    @FXML private Label notificationLabel;

    private ControllerFactory factory;
    private String operatorUsername;
    private AccessNotificationController notificationController;

    /** Inizializza la dashboard con l'operatore autenticato. */
    public void initData(ControllerFactory factory, String username) {
        this.factory = factory;
        this.operatorUsername = username;
        this.notificationController = factory.createAccessNotificationController();
        this.welcomeLabel.setText("Bentornato, " + username + "!");
        Platform.runLater(this::showAccessNotifications);
    }

    @FXML
    private void handleMyGroups(Event event) {
        showFeatureInProgress(
                "I miei gruppi",
                "Qui potrai visualizzare i gruppi ai quali sei iscritto e i beni che puoi prenotare."
        );
    }

    @FXML
    private void handleJoinGroup(Event event) {
        MainAppGUI.replaceScene("/view/RequestGroupAccess.fxml",
                (RequestGroupAccessGraphicController ctrl) ->
                        ctrl.initData(factory, operatorUsername));
    }

    @FXML
    private void handleBookings(Event event) {
        showFeatureInProgress(
                "Le mie prenotazioni",
                "Qui potrai consultare e gestire le prenotazioni dei beni."
        );
    }

    @FXML
    private void handleLogout(Event event) {
        try {
            MainAppGUI.showLogin();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore",
                    "Impossibile tornare alla schermata di login.");
        }
    }

    private void showAccessNotifications() {
        try {
            UserBean operatorBean = new UserBean(operatorUsername, Role.OPERATOR);
            List<MembershipRequestBean> notifications =
                    notificationController.getUnreadResults(operatorBean);

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
                notificationController.markAsRead(notification, operatorBean);
            }
            showAlert(Alert.AlertType.INFORMATION, "Esito richieste di accesso",
                    message.toString().trim());
        } catch (Exception e) {
            notificationLabel.setText("Impossibile caricare le notifiche.");
        }
    }

    private void showFeatureInProgress(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title,
                message + "\n\nOperatore: " + operatorUsername);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
