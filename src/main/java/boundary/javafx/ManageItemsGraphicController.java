package boundary.javafx;

import boundary.javafx.navigation.SceneNavigator;
import controller.ManageItemsController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.bean.CreateItemBean;
import model.bean.GroupBean;
import model.bean.ItemBean;
import model.bean.Role;
import model.bean.UserBean;

/** Boundary JavaFX del caso d'uso di visualizzazione e creazione degli item. */
public class ManageItemsGraphicController {
    @FXML private Label groupNameLabel;
    @FXML private TableView<ItemBean> itemsTable;
    @FXML private TableColumn<ItemBean, Integer> idColumn;
    @FXML private TableColumn<ItemBean, String> nameColumn;
    @FXML private TableColumn<ItemBean, Integer> priorityColumn;
    @FXML private TableColumn<ItemBean, Integer> maxUsageTimeColumn;
    @FXML private TableColumn<ItemBean, String> statusColumn;
    @FXML private TextField itemNameField;
    @FXML private Spinner<Integer> prioritySpinner;
    @FXML private Spinner<Integer> maxUsageTimeSpinner;

    private SceneNavigator navigator;
    private String adminUsername;
    private ManageItemsController logicController;

    public void initData(ManageItemsController logicController,
                         SceneNavigator navigator,
                         GroupBean group,
                         String adminUsername) {
        this.logicController = logicController;
        this.navigator = navigator;
        this.adminUsername = adminUsername;
        groupNameLabel.setText("Item del gruppo: " + group.getGroupName());

        configureInputs();
        configureTable();
        loadItems();
    }

    private void configureInputs() {
        prioritySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));
        maxUsageTimeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, Integer.MAX_VALUE, 60, 15));
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        maxUsageTimeColumn.setCellValueFactory(new PropertyValueFactory<>("maxUsageTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        itemsTable.setPlaceholder(new Label("Non ci sono item in questo gruppo."));
    }

    private void loadItems() {
        try {
            itemsTable.setItems(FXCollections.observableArrayList(
                    logicController.getItemList(adminBean())));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Impossibile caricare gli item", e.getMessage());
        }
    }

    @FXML
    private void handleAddItem() {
        try {
            CreateItemBean item = new CreateItemBean(
                    itemNameField.getText(),
                    prioritySpinner.getValue(),
                    maxUsageTimeSpinner.getValue());
            logicController.createItem(item, adminBean());
            itemNameField.clear();
            loadItems();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Impossibile aggiungere l'item", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        navigator.showManageGroups(adminUsername);
    }

    @FXML
    private void handleLogout() {
        navigator.showLogin();
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
