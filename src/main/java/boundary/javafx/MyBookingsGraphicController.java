package boundary.javafx;

import boundary.javafx.navigation.SceneNavigator;
import controller.MyBookingsController;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.bean.BookingBean;
import model.bean.Role;
import model.bean.UserBean;

/** Boundary JavaFX del caso d'uso "Le mie prenotazioni". */
public class MyBookingsGraphicController {
    @FXML private TableView<BookingBean> bookingsTable;
    @FXML private TableColumn<BookingBean, String> groupColumn;
    @FXML private TableColumn<BookingBean, String> itemColumn;
    @FXML private TableColumn<BookingBean, String> dateColumn;
    @FXML private TableColumn<BookingBean, String> timeColumn;
    @FXML private TableColumn<BookingBean, String> durationColumn;
    @FXML private TableColumn<BookingBean, String> deletableColumn;
    @FXML private Button deleteButton;
    @FXML private Label selectionLabel;

    private MyBookingsController logicController;
    private SceneNavigator navigator;
    private String operatorUsername;

    public void initData(MyBookingsController logicController, SceneNavigator navigator,
                         String operatorUsername) {
        this.logicController = logicController;
        this.navigator = navigator;
        this.operatorUsername = operatorUsername;
        configureTable();
        loadBookings();
    }

    private void configureTable() {
        groupColumn.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeRangeLabel"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationLabel"));
        deletableColumn.setCellValueFactory(new PropertyValueFactory<>("deletionLabel"));
        bookingsTable.setPlaceholder(new Label("Non hai prenotazioni registrate."));
        bookingsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldBooking, newBooking) -> updateSelection(newBooking));
        updateSelection(null);
    }

    private void loadBookings() {
        try {
            bookingsTable.setItems(FXCollections.observableArrayList(
                    logicController.getMyBookings(operatorBean())));
            bookingsTable.getSelectionModel().clearSelection();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Errore",
                    "Impossibile caricare le prenotazioni: " + e.getMessage());
        }
    }

    private void updateSelection(BookingBean booking) {
        boolean canDelete = booking != null && booking.isDeletable();
        deleteButton.setDisable(!canDelete);
        if (booking == null) {
            selectionLabel.setText("Seleziona una prenotazione futura da eliminare.");
        } else if (canDelete) {
            selectionLabel.setText("La prenotazione selezionata non è ancora iniziata.");
        } else {
            selectionLabel.setText(
                    "La prenotazione è già iniziata o conclusa e deve restare nello storico.");
        }
    }

    @FXML
    private void handleDelete(Event event) {
        BookingBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isDeletable()) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Vuoi eliminare la prenotazione di '" + selected.getItemName()
                        + "' del " + selected.getDateLabel() + "?",
                ButtonType.CANCEL, ButtonType.OK);
        confirmation.setTitle("Conferma eliminazione");
        confirmation.setHeaderText("Lo slot tornerà immediatamente disponibile.");
        if (confirmation.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }

        try {
            logicController.deleteBooking(selected.getBookingId(), operatorBean());
            bookingsTable.getItems().remove(selected);
            updateSelection(null);
            showAlert(Alert.AlertType.INFORMATION, "Prenotazione eliminata",
                    "La prenotazione è stata rimossa e lo slot è nuovamente disponibile.");
        } catch (Exception e) {
            loadBookings();
            showAlert(Alert.AlertType.ERROR, "Impossibile eliminare la prenotazione",
                    e.getMessage());
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
    private void handleJoinGroup(Event event) {
        navigator.showRequestGroupAccess(operatorUsername);
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
