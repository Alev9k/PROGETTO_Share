package boundary.javafx;

import boundary.javafx.navigation.SceneNavigator;
import controller.MyBookingsController;
import controller.ReturnItemController;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.bean.BookingBean;
import model.entity.ReturnCondition;

/** Boundary JavaFX dei casi d'uso sulle prenotazioni dell'operatore. */
public class MyBookingsGraphicController {
    @FXML private TableView<BookingBean> bookingsTable;
    @FXML private TableColumn<BookingBean, String> groupColumn;
    @FXML private TableColumn<BookingBean, String> itemColumn;
    @FXML private TableColumn<BookingBean, String> dateColumn;
    @FXML private TableColumn<BookingBean, String> timeColumn;
    @FXML private TableColumn<BookingBean, String> durationColumn;
    @FXML private TableColumn<BookingBean, String> deletableColumn;
    @FXML private TableColumn<BookingBean, String> returnColumn;
    @FXML private Button deleteButton;
    @FXML private Button returnButton;
    @FXML private Label selectionLabel;

    private MyBookingsController logicController;
    private ReturnItemController returnItemController;
    private SceneNavigator navigator;

    public void initData(MyBookingsController logicController,
                         ReturnItemController returnItemController,
                         SceneNavigator navigator) {
        this.logicController = logicController;
        this.returnItemController = returnItemController;
        this.navigator = navigator;
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
        returnColumn.setCellValueFactory(new PropertyValueFactory<>("returnLabel"));
        bookingsTable.setPlaceholder(new Label("Non hai prenotazioni registrate."));
        bookingsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldBooking, newBooking) -> updateSelection(newBooking));
        updateSelection(null);
    }

    private void loadBookings() {
        try {
            bookingsTable.setItems(FXCollections.observableArrayList(
                    logicController.getMyBookings()));
            bookingsTable.getSelectionModel().clearSelection();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Errore",
                    "Impossibile caricare le prenotazioni: " + e.getMessage());
        }
    }

    private void updateSelection(BookingBean booking) {
        boolean canDelete = booking != null && booking.isDeletable();
        boolean canReturn = booking != null && booking.isReturnable();
        deleteButton.setDisable(!canDelete);
        returnButton.setDisable(!canReturn);
        if (booking == null) {
            selectionLabel.setText("Seleziona una prenotazione da gestire.");
        } else if (canDelete) {
            selectionLabel.setText("La prenotazione non è ancora iniziata e può essere eliminata.");
        } else if (canReturn) {
            selectionLabel.setText("Puoi riconsegnare l'item entro la fine della prenotazione.");
        } else if (booking.getReturnCondition() != ReturnCondition.NOT_REPORTED) {
            selectionLabel.setText("La riconsegna è già stata registrata.");
        } else {
            selectionLabel.setText("La prenotazione è scaduta e deve restare nello storico.");
        }
    }

    @FXML
    private void handleReturn(Event event) {
        BookingBean selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isReturnable()) {
            return;
        }

        ButtonType intactButton = new ButtonType("INTATTO", ButtonBar.ButtonData.OK_DONE);
        ButtonType brokenButton = new ButtonType("GUASTO", ButtonBar.ButtonData.OTHER);
        Alert conditionDialog = new Alert(Alert.AlertType.CONFIRMATION,
                "Indica le condizioni dell'item riconsegnato.",
                ButtonType.CANCEL, intactButton, brokenButton);
        conditionDialog.setTitle("Riconsegna item");
        conditionDialog.setHeaderText(selected.getItemName() + " - "
                + selected.getGroupName());
        ButtonType choice = conditionDialog.showAndWait().orElse(ButtonType.CANCEL);
        if (choice == ButtonType.CANCEL) {
            return;
        }

        boolean broken = choice == brokenButton;
        if (broken && !confirmBrokenItem(selected)) {
            return;
        }

        try {
            returnItemController.returnItem(selected.getBookingId(), broken);
            loadBookings();
            showAlert(Alert.AlertType.INFORMATION, "Riconsegna registrata",
                    broken
                            ? "L'item è stato segnalato guasto. L'Admin e gli operatori con prenotazioni future saranno avvisati."
                            : "L'item è stato riconsegnato intatto.");
        } catch (Exception e) {
            loadBookings();
            showAlert(Alert.AlertType.ERROR, "Impossibile registrare la riconsegna",
                    e.getMessage());
        }
    }

    private boolean confirmBrokenItem(BookingBean booking) {
        Alert confirmation = new Alert(Alert.AlertType.WARNING,
                "Segnalando l'item come guasto, tutte le sue prenotazioni future saranno eliminate.",
                ButtonType.CANCEL, ButtonType.OK);
        confirmation.setTitle("Conferma segnalazione guasto");
        confirmation.setHeaderText("Confermi che '" + booking.getItemName() + "' è guasto?");
        return confirmation.showAndWait().filter(ButtonType.OK::equals).isPresent();
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
            logicController.deleteBooking(selected.getBookingId());
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
        navigator.showDashboard();
    }

    @FXML
    private void handleMyGroups(Event event) {
        navigator.showMyGroups();
    }

    @FXML
    private void handleJoinGroup(Event event) {
        navigator.showRequestGroupAccess();
    }

    @FXML
    private void handleLogout(Event event) {
        navigator.logout();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
