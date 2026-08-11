package boundary.javafx;

import boundary.javafx.navigation.SceneNavigator;
import controller.AccessNotificationController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.event.Event;
import model.bean.Role;
import model.bean.UserBean;

public class AdminDashboardGraphicController {

    @FXML private Label welcomeLabel;
    @FXML private Label pendingRequestsLabel;

    private SceneNavigator navigator;
    private String adminUsername;

    /**
     * Inizializza la Dashboard.
     */
    public void initData(AccessNotificationController notificationController,
                         SceneNavigator navigator, String username) {
        this.navigator = navigator;
        this.adminUsername = username;

        // CORREZIONE 1: Testo di benvenuto
        this.welcomeLabel.setText("Bentornato, " + username + "!");
        try {
            long pendingCount = notificationController.countPendingForAdmin(
                    new UserBean(username, Role.ADMIN));
            this.pendingRequestsLabel.setText(pendingCount == 1
                    ? "Hai 1 richiesta di accesso in attesa."
                    : "Hai " + pendingCount + " richieste di accesso in attesa.");
        } catch (Exception e) {
            this.pendingRequestsLabel.setText("Impossibile caricare le notifiche.");
        }
    }

    // CORREZIONE 2: Usiamo "Event" invece di "ActionEvent" così
    // funziona sia con i Button (sidebar) che con i VBox (cards)
    @FXML
    private void handleManageGroups(Event event) {
        try {
            navigator.showManageGroups(adminUsername);
        } catch (Exception e) {
            showError("Errore", "Impossibile caricare la gestione gruppi.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateGroup(Event event) {
        try {
            navigator.showCreateGroup(adminUsername);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(Event event) {
        try {
            navigator.showLogin();
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
