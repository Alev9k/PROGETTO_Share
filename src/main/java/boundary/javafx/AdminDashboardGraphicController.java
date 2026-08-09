package boundary.javafx;

import controller.ControllerFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.event.Event;

public class AdminDashboardGraphicController {

    @FXML private Label welcomeLabel;
    @FXML private Label pendingRequestsLabel;

    private ControllerFactory factory;
    private String adminUsername;

    /**
     * Inizializza la Dashboard.
     */
    public void initData(ControllerFactory factory, String username) {
        this.factory = factory;
        this.adminUsername = username;

        // CORREZIONE 1: Testo di benvenuto
        this.welcomeLabel.setText("Bentornato, " + username + "!");
        try {
            long pendingCount = factory.createAccessNotificationController()
                    .countPendingForAdmin(new model.bean.UserBean(username, model.bean.Role.ADMIN));
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
            MainAppGUI.replaceScene("/view/ManageGroup.fxml", (ManageGroupGraphicController ctrl) ->
                    ctrl.initData(factory, adminUsername)
            );
        } catch (Exception e) {
            showError("Errore", "Impossibile caricare la gestione gruppi.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateGroup(Event event) {
        try {
            MainAppGUI.replaceScene("/view/CreateGroup.fxml", (CreateGroupGraphicController ctrl) ->
                    ctrl.initData(factory, adminUsername));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateAsset(Event event) {
        showInfo("Nuovo Asset", "Funzionalità di creazione asset in arrivo!");
    }

    @FXML
    private void handleLogout(Event event) {
        try {
            MainAppGUI.showLogin();
        } catch (Exception e) {
            showError("Errore", "Impossibile tornare al login.");
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
