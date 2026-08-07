package boundary.javafx;

import controller.ControllerFactory;
import controller.ManageItemsController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.bean.GroupBean;
import model.bean.ItemBean;

/** Boundary JavaFX del caso d'uso di gestione degli item di un gruppo. */
public class ManageItemsGraphicController {
    @FXML private Label groupNameLabel;
    @FXML private TableView<ItemBean> itemsTable;
    @FXML private TableColumn<ItemBean, String> nameColumn;
    @FXML private TableColumn<ItemBean, Integer> priorityColumn;
    @FXML private TableColumn<ItemBean, Integer> maxUsageTimeColumn;
    @FXML private TextField itemNameField;
    @FXML private TextField priorityField;
    @FXML private TextField maxUsageTimeField;

    private ControllerFactory factory;
    private GroupBean group;
    private String adminUsername;
    private ManageItemsController logicController;

    public void initData(ControllerFactory factory, GroupBean group, String adminUsername) {
        this.factory = factory;
        this.group = group;
        this.adminUsername = adminUsername;
        this.groupNameLabel.setText("Item del gruppo: " + group.getGroupName());

        try {
            this.logicController = factory.createManageItemsController(group.getGroupId());
            configureTable();
            loadItems();
        } catch (Exception e) {
            showError("Errore", "Impossibile aprire la gestione item: " + e.getMessage());
        }
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        maxUsageTimeColumn.setCellValueFactory(new PropertyValueFactory<>("maxUsageTime"));
        itemsTable.setPlaceholder(new Label("Non ci sono item in questo gruppo."));
    }

    private void loadItems() {
        itemsTable.setItems(FXCollections.observableArrayList(logicController.getItemList()));
    }

    @FXML
    private void handleAddItem() {
        try {
            int priority = Integer.parseInt(priorityField.getText().trim());
            int maxUsageTime = Integer.parseInt(maxUsageTimeField.getText().trim());
            logicController.addNewItem(new ItemBean(itemNameField.getText(), priority, maxUsageTime));
            itemNameField.clear();
            priorityField.clear();
            maxUsageTimeField.clear();
            loadItems();
        } catch (NumberFormatException e) {
            showWarning("Dati non validi", "Priorit\u00e0 e tempo massimo devono essere numeri interi.");
        } catch (Exception e) {
            showError("Impossibile aggiungere l'item", e.getMessage());
        }
    }

    @FXML
    private void handleRemoveItem() {
        ItemBean selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selezione mancante", "Seleziona un item da rimuovere.");
            return;
        }
        try {
            logicController.removeItem(selected);
            loadItems();
        } catch (Exception e) {
            showError("Impossibile rimuovere l'item", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        MainAppGUI.replaceScene("/view/ManageGroup.fxml", (ManageGroupGraphicController ctrl) ->
                ctrl.initData(factory, adminUsername));
    }

    @FXML
    private void handleLogout() {
        try {
            MainAppGUI.showLogin();
        } catch (Exception e) {
            showError("Errore", "Impossibile tornare al login.");
        }
    }

    private void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
