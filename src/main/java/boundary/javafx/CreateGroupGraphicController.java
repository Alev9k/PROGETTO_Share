package boundary.javafx;

import controller.ControllerFactory;
import controller.CreateGroupController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import model.bean.GroupBean;
import model.bean.Role;
import model.bean.UserBean;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class CreateGroupGraphicController {

    @FXML private TextField groupNameField;
    @FXML private ChoiceBox<String> openTimeChoice;
    @FXML private ChoiceBox<String> closeTimeChoice;

    private CreateGroupController logicController;
    private ControllerFactory factory;
    private String adminUsername;

    /**
     * Metodo chiamato automaticamente da JavaFX all'avvio della schermata.
     * Lo usiamo per riempire le ChoiceBox con gli orari.
     */
    @FXML
    public void initialize() {
        // Generiamo orari dalle 00:00 alle 23:00 per popolare le tendine
        for (int i = 0; i <= 23; i++) {
            String timeStr = String.format("%02d:00", i);
            openTimeChoice.getItems().add(timeStr);
            closeTimeChoice.getItems().add(timeStr);
        }

        // Valori di default
        openTimeChoice.setValue("08:00");
        closeTimeChoice.setValue("18:00");
    }

    /**
     * Chiamato da MainAppGUI per iniettare i dati.
     */
    public void initData(ControllerFactory factory, String username) {
        this.factory = factory;
        this.adminUsername = username;

        try {
            // Chiediamo alla factory di creare il controller applicativo
            this.logicController = factory.createCreateGroupController();
        } catch (Exception e) {
            e.printStackTrace(); // Stampa l'errore tecnico in console
            showError("Errore di Inizializzazione", "Impossibile caricare i dati del sistema: " + e.getMessage());
        }
    }
    // --- AZIONE PRINCIPALE ---

    @FXML
    private void submitCreateGroup(Event event) {
        String name = groupNameField.getText();
        String openTimeStr = openTimeChoice.getValue();
        String closeTimeStr = closeTimeChoice.getValue();

        if (name.isEmpty()) {
            showError("Errore", "Il nome del gruppo non può essere vuoto.");
            return;
        }

        try {
            LocalTime openTime = LocalTime.parse(openTimeStr);
            LocalTime closeTime = LocalTime.parse(closeTimeStr);

            // 1. IMPACCHETTIAMO I DATI NEI BEAN
            GroupBean newGroupBean = new GroupBean(name, openTime, closeTime);
            UserBean adminBean = new UserBean(adminUsername, Role.ADMIN);

            // 2. PASSIAMO I BEAN AL CONTROLLER LOGICO
            GroupBean createdGroup = logicController.createGroup(newGroupBean, adminBean);

            showInfo("Gruppo creato",
                    "Gruppo '" + name + "' creato correttamente!\n\n" +
                    "Token di accesso: " + createdGroup.getAccessToken() + "\n\n" +
                    "Comunica questo token agli operatori che vuoi invitare.");
            handleManageGroups(event);

        } catch (java.time.format.DateTimeParseException e) {
            showError("Errore Formato", "Formato dell'orario non valido.");
        } catch (Exception e) {
            showError("Errore di Creazione", e.getMessage());
        }
    }

    // --- NAVIGAZIONE SIDEBAR ---

    @FXML
    private void handleBackToDashboard(Event event) {
        try {
            MainAppGUI.showDashboard(new UserBean(adminUsername, Role.ADMIN));
        } catch (Exception e) {
            showError("Errore Navigazione", "Impossibile tornare alla Dashboard.");
        }
    }

    @FXML
    private void handleManageGroups(Event event) {
        try {
            MainAppGUI.replaceScene("/view/ManageGroup.fxml", (ManageGroupGraphicController ctrl) ->
                    ctrl.initData(factory, adminUsername));
        } catch (Exception e) {
            showError("Errore Navigazione", "Impossibile caricare la gestione gruppi.");
        }
    }

    @FXML
    private void handleCreateAsset(Event event) {
        showInfo("Nuovo Asset", "Funzionalità in fase di sviluppo.");
    }

    @FXML
    private void handleLogout(Event event) {
        try {
            MainAppGUI.showLogin();
        } catch (Exception e) {
            showError("Errore", "Impossibile effettuare il logout.");
        }
    }

    // --- UTILITY ---

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
