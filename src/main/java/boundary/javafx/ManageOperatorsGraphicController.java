package boundary.javafx;

import controller.ControllerFactory;
import controller.ManageOperatorsController;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.bean.GroupBean;
import model.bean.MembershipRequestBean;
import model.bean.OperatorBean;
import model.bean.Role;
import model.bean.UserBean;

public class ManageOperatorsGraphicController {
    @FXML private Label groupNameLabel;
    @FXML private TableView<MembershipRequestBean> pendingRequestsTable;
    @FXML private TableColumn<MembershipRequestBean, String> requestOperatorColumn;
    @FXML private TableColumn<MembershipRequestBean, String> requestDateColumn;
    @FXML private TableView<OperatorBean> membersTable;
    @FXML private TableColumn<OperatorBean, String> memberUsernameColumn;
    @FXML private TableColumn<OperatorBean, String> memberStatusColumn;

    private ControllerFactory factory;
    private ManageOperatorsController logicController;
    private GroupBean group;
    private String adminUsername;

    public void initData(ControllerFactory factory, GroupBean group, String adminUsername) {
        this.factory = factory;
        this.group = group;
        this.adminUsername = adminUsername;
        this.groupNameLabel.setText("Gestione membri: " + group.getGroupName());

        try {
            this.logicController = factory.createManageOperatorsController(group.getGroupId());
            configureTables();
            reloadData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore di inizializzazione", e.getMessage());
        }
    }

    private void configureTables() {
        requestOperatorColumn.setCellValueFactory(new PropertyValueFactory<>("operatorUsername"));
        requestDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtLabel"));
        memberUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        memberStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        pendingRequestsTable.setPlaceholder(new Label("Nessuna richiesta di accesso in attesa."));
        membersTable.setPlaceholder(new Label("Nessun operatore appartiene al gruppo."));
    }

    private void reloadData() {
        if (logicController == null) {
            return;
        }
        try {
            pendingRequestsTable.setItems(FXCollections.observableArrayList(
                    logicController.getPendingRequests(adminBean())));
            membersTable.setItems(FXCollections.observableArrayList(
                    logicController.getOperatorList()));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile caricare i membri: " + e.getMessage());
        }
    }

    @FXML
    private void handleAcceptRequest(Event event) {
        MembershipRequestBean selected = pendingRequestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selezione mancante",
                    "Seleziona una richiesta da accettare.");
            return;
        }
        try {
            logicController.acceptRequest(selected, adminBean());
            reloadData();
            showAlert(Alert.AlertType.INFORMATION, "Richiesta accettata",
                    selected.getOperatorUsername() + " è ora membro del gruppo.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Impossibile accettare la richiesta", e.getMessage());
        }
    }

    @FXML
    private void handleRejectRequest(Event event) {
        MembershipRequestBean selected = pendingRequestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selezione mancante",
                    "Seleziona una richiesta da rifiutare.");
            return;
        }
        try {
            logicController.rejectRequest(selected, adminBean());
            reloadData();
            showAlert(Alert.AlertType.INFORMATION, "Richiesta rifiutata",
                    "La richiesta di " + selected.getOperatorUsername() + " è stata rifiutata.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Impossibile rifiutare la richiesta", e.getMessage());
        }
    }

    @FXML
    private void handleToggleBlock(Event event) {
        OperatorBean selected = membersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selezione mancante",
                    "Seleziona un membro da bloccare o riattivare.");
            return;
        }
        try {
            logicController.toggleBlock(selected);
            reloadData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Impossibile modificare lo stato", e.getMessage());
        }
    }

    @FXML
    private void handleBackToGroups(Event event) {
        MainAppGUI.replaceScene("/view/ManageGroup.fxml", (ManageGroupGraphicController ctrl) ->
                ctrl.initData(factory, adminUsername));
    }

    @FXML
    private void handleBackToDashboard(Event event) {
        MainAppGUI.showDashboard(adminBean());
    }

    @FXML
    private void handleCreateGroup(Event event) {
        MainAppGUI.replaceScene("/view/CreateGroup.fxml", (CreateGroupGraphicController ctrl) ->
                ctrl.initData(factory, adminUsername));
    }

    @FXML
    private void handleLogout(Event event) {
        try {
            MainAppGUI.showLogin();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile effettuare il logout.");
        }
    }

    private UserBean adminBean() {
        return new UserBean(adminUsername, Role.ADMIN);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
