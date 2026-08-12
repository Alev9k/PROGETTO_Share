package boundary.javafx;

import boundary.javafx.navigation.SceneNavigator;
import controller.JoinGroupController;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import model.bean.MembershipRequestBean;
import model.bean.Role;
import model.bean.UserBean;

public class RequestGroupAccessGraphicController {
    @FXML private TextField tokenField;
    @FXML private TableView<MembershipRequestBean> requestsTable;
    @FXML private TableColumn<MembershipRequestBean, String> groupColumn;
    @FXML private TableColumn<MembershipRequestBean, String> statusColumn;
    @FXML private TableColumn<MembershipRequestBean, String> dateColumn;

    private SceneNavigator navigator;
    private JoinGroupController logicController;
    private String operatorUsername;

    public void initData(JoinGroupController logicController,
                         SceneNavigator navigator,
                         String operatorUsername) {
        this.logicController = logicController;
        this.navigator = navigator;
        this.operatorUsername = operatorUsername;
        configureTokenField();
        configureTable();
        loadRequests();
    }

    private void configureTokenField() {
        tokenField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,6}") ? change : null));
    }

    private void configureTable() {
        groupColumn.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtLabel"));
        requestsTable.setPlaceholder(new Label("Non hai ancora inviato richieste di accesso."));
    }

    @FXML
    private void handleSubmit(Event event) {
        try {
            MembershipRequestBean request = logicController.requestAccess(
                    tokenField.getText(), operatorBean());
            tokenField.clear();
            loadRequests();
            showAlert(Alert.AlertType.INFORMATION, "Richiesta inviata",
                    "La richiesta per il gruppo '" + request.getGroupName() +
                            "' è ora in attesa dell'approvazione dell'Admin.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Impossibile inviare la richiesta", e.getMessage());
        }
    }

    private void loadRequests() {
        try {
            requestsTable.setItems(FXCollections.observableArrayList(
                    logicController.getRequestHistory(operatorBean())));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore",
                    "Impossibile caricare le richieste: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard(Event event) {
        navigator.showDashboard(operatorBean());
    }

    @FXML
    private void handleMyGroups(Event event) {
        navigator.showMyGroups(operatorUsername);
    }

    @FXML
    private void handleBookings(Event event) {
        navigator.showMyBookings(operatorUsername);
    }

    @FXML
    private void handleLogout(Event event) {
        navigator.showLogin();
    }

    private UserBean operatorBean() {
        return new UserBean(operatorUsername, Role.OPERATOR);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
