package boundary.javafx;

import controller.ControllerFactory;
import controller.ManageGroupController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event; // Cambiato in Event per flessibilità
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.bean.GroupBean;
import model.bean.Role;
import model.bean.UserBean;
import java.util.List;

public class ManageGroupGraphicController {

    @FXML private TableView<GroupBean> groupsTable;
    @FXML private TableColumn<GroupBean, String> nameColumn;
    @FXML private TableColumn<GroupBean, Integer> idColumn;

    private ManageGroupController logicController;
    private ControllerFactory factory;
    private String adminUsername;

    /**
     * Inizializza il controller grafico.
     * Viene chiamato dal View Handler centrale (MainAppGUI).
     */
    public void initData(ControllerFactory factory, String username) {
        this.factory = factory;
        this.adminUsername = username;
        this.logicController = factory.createManageGroupController();

        setupTable();
        loadGroups();
    }

    private void setupTable() {
        // Colleghiamo le colonne ai campi del GroupBean
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("groupId"));

        // Gestione lista vuota tramite segnaposto grafico
        Label emptyLabel = new Label("Non ci sono gruppi associati al tuo account.");
        emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
        groupsTable.setPlaceholder(emptyLabel);
    }

    private void loadGroups() {
        try {
            List<GroupBean> groups = logicController.getGroupList(adminUsername);
            ObservableList<GroupBean> observableList = FXCollections.observableArrayList(groups);
            groupsTable.setItems(observableList);
        } catch (Exception e) {
            showError("Errore", "Impossibile recuperare i gruppi: " + e.getMessage());
        }
    }

    // --- AZIONI DELLA TABELLA (Pulsanti in basso a destra) ---

    @FXML
    private void handleManageOperators(Event event) {
        GroupBean selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selezione mancante", "Seleziona un gruppo per gestire i membri.");
            return;
        }
        MainAppGUI.replaceScene("/view/ManageOperators.fxml", (ManageOperatorsGraphicController ctrl) ->
                ctrl.initData(factory, selected, adminUsername));
    }

    @FXML
    private void handleManageItems(Event event) {
        GroupBean selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selezione mancante", "Seleziona un gruppo per gestire i beni.");
            return;
        }
        MainAppGUI.replaceScene("/view/ManageItems.fxml", (ManageItemsGraphicController ctrl) ->
                ctrl.initData(factory, selected, adminUsername));
    }

    @FXML
    private void handleDeleteGroup(Event event) {
        GroupBean selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selezione mancante", "Seleziona un gruppo da eliminare.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Sei sicuro di voler eliminare il gruppo " + selected.getGroupName() + "?");
        confirm.setTitle("Conferma Eliminazione");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // 1. Prepariamo il Bean dell'Admin corrente
                    UserBean adminBean = new UserBean(adminUsername, Role.ADMIN);

                    // 2. Passiamo i due Bean al controller logico
                    logicController.deleteGroup(selected, adminBean);

                    showInfo("Successo", "Gruppo eliminato correttamente.");

                    // 3. Ricarichiamo la tabella: ora il gruppo rimosso non ci sarà più!
                    loadGroups();
                } catch (Exception e) {
                    showError("Errore eliminazione", e.getMessage());
                }
            }
        });
    }

    // --- AZIONI DELLA SIDEBAR (Navigazione) ---

    @FXML
    private void handleBackToDashboard(Event event) {
        try {
            // Sfruttiamo il View Handler centralizzato per tornare alla Home dell'admin
            MainAppGUI.showDashboard(new UserBean(adminUsername, Role.ADMIN));
        } catch (Exception e) {
            showError("Errore Navigazione", "Impossibile tornare alla Dashboard.");
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
        showInfo("Nuovo Asset", "Modulo di definizione dei beni in fase di sviluppo.");
    }

    @FXML
    private void handleLogout(Event event) {
        try {
            MainAppGUI.showLogin();
        } catch (Exception e) {
            showError("Errore", "Impossibile tornare alla schermata di login.");
        }
    }

    // --- Utility per i Dialogs ---
    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

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
