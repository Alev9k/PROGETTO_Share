package boundary.javafx;

import boundary.javafx.navigation.SceneNavigator;
import controller.BookItemController;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.bean.BookingBean;
import model.bean.BookingAvailabilityBean;
import model.bean.BookingRequestBean;
import model.bean.ItemBean;
import model.bean.OperatorGroupBean;
import model.bean.Role;
import model.bean.UserBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Boundary JavaFX del caso d'uso "Prenota item". */
public class MyGroupsGraphicController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private ComboBox<OperatorGroupBean> groupChoice;
    @FXML private TableView<ItemBean> itemsTable;
    @FXML private TableColumn<ItemBean, String> nameColumn;
    @FXML private TableColumn<ItemBean, Integer> priorityColumn;
    @FXML private TableColumn<ItemBean, Integer> maxUsageColumn;
    @FXML private TableColumn<ItemBean, String> statusColumn;
    @FXML private ChoiceBox<LocalDate> dateChoice;
    @FXML private ChoiceBox<LocalTime> startChoice;
    @FXML private ChoiceBox<Integer> durationChoice;
    @FXML private Label availabilityLabel;
    @FXML private Button bookButton;

    private BookItemController logicController;
    private SceneNavigator navigator;
    private String operatorUsername;
    private BookingAvailabilityBean currentAvailability;

    public void initData(BookItemController logicController, SceneNavigator navigator,
                         String operatorUsername) {
        this.logicController = logicController;
        this.navigator = navigator;
        this.operatorUsername = operatorUsername;
        configureControls();
        loadGroups();
    }

    private void configureControls() {
        groupChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(OperatorGroupBean group) {
                return formatGroup(group);
            }

            @Override
            public OperatorGroupBean fromString(String value) {
                return null;
            }
        });
        groupChoice.setCellFactory(listView -> createGroupCell());
        groupChoice.setButtonCell(createGroupCell());
        dateChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (date == null) {
                    return "";
                }
                List<LocalDate> dates = logicController.getBookableDates();
                String prefix = date.equals(dates.get(0)) ? "Oggi" : "Domani";
                return prefix + " · " + DATE_FORMAT.format(date);
            }

            @Override
            public LocalDate fromString(String value) {
                return null;
            }
        });
        startChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalTime time) {
                return time == null ? "" : TIME_FORMAT.format(time);
            }

            @Override
            public LocalTime fromString(String value) {
                return null;
            }
        });
        durationChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer minutes) {
                return minutes == null ? "" : minutes + " minuti";
            }

            @Override
            public Integer fromString(String value) {
                return null;
            }
        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        maxUsageColumn.setCellValueFactory(new PropertyValueFactory<>("maxUsageTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        itemsTable.setPlaceholder(new Label("Nessun item prenotabile nel gruppo selezionato."));

        groupChoice.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldGroup, newGroup) -> {
                    if (newGroup != null && !newGroup.isActive()) {
                        groupChoice.getSelectionModel().clearSelection();
                        availabilityLabel.setText(
                                "Il gruppo è visibile, ma la tua membership è bloccata.");
                        loadItems(null);
                    } else {
                        loadItems(newGroup);
                    }
                });
        itemsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldItem, newItem) -> refreshStartSlots());
        dateChoice.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldDate, newDate) -> refreshStartSlots());
        startChoice.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldStart, newStart) -> refreshDurations());

        List<LocalDate> dates = logicController.getBookableDates();
        dateChoice.setItems(FXCollections.observableArrayList(dates));
        dateChoice.getSelectionModel().selectFirst();
    }

    private void loadGroups() {
        try {
            List<OperatorGroupBean> groups = logicController.getMyGroups(operatorBean());
            groupChoice.setItems(FXCollections.observableArrayList(groups));
            if (groups.isEmpty()) {
                availabilityLabel.setText(
                        "Non appartieni ancora a un gruppo. Invia prima una richiesta di accesso.");
                updateBookingAvailability();
            } else {
                groups.stream()
                        .filter(OperatorGroupBean::isActive)
                        .findFirst()
                        .ifPresentOrElse(
                                groupChoice.getSelectionModel()::select,
                                () -> {
                                    availabilityLabel.setText(
                                            "Le tue membership sono attualmente bloccate.");
                                    updateBookingAvailability();
                                });
            }
        } catch (RuntimeException e) {
            showError("Impossibile caricare i gruppi", e);
        }
    }

    private void loadItems(OperatorGroupBean group) {
        itemsTable.getItems().clear();
        clearSlots();
        if (group == null) {
            return;
        }
        try {
            List<ItemBean> items = logicController.getBookableItems(
                    group.getGroupId(), operatorBean());
            itemsTable.setItems(FXCollections.observableArrayList(items));
            if (!items.isEmpty()) {
                itemsTable.getSelectionModel().selectFirst();
            } else {
                availabilityLabel.setText("Il gruppo non contiene item prenotabili.");
                updateBookingAvailability();
            }
        } catch (RuntimeException e) {
            showError("Impossibile caricare gli item", e);
        }
    }

    private void refreshStartSlots() {
        clearSlots();
        OperatorGroupBean group = groupChoice.getValue();
        ItemBean item = itemsTable.getSelectionModel().getSelectedItem();
        LocalDate date = dateChoice.getValue();
        if (group == null || item == null || date == null) {
            updateBookingAvailability();
            return;
        }
        try {
            currentAvailability = logicController.getAvailability(
                    group.getGroupId(), item.getItemId(), date, operatorBean());
            List<LocalTime> slots = currentAvailability.getStartSlots();
            startChoice.setItems(FXCollections.observableArrayList(slots));
            if (slots.isEmpty()) {
                availabilityLabel.setText("Non ci sono slot liberi per la selezione corrente.");
            } else {
                availabilityLabel.setText(slots.size() == 1
                        ? "È disponibile uno slot di partenza."
                        : "Sono disponibili " + slots.size() + " slot di partenza.");
                startChoice.getSelectionModel().selectFirst();
            }
            updateBookingAvailability();
        } catch (RuntimeException e) {
            showError("Impossibile calcolare gli slot", e);
        }
    }

    private void refreshDurations() {
        durationChoice.getItems().clear();
        LocalTime start = startChoice.getValue();
        if (currentAvailability == null || start == null) {
            updateBookingAvailability();
            return;
        }
        List<Integer> durations = currentAvailability.getDurationsFor(start);
        durationChoice.setItems(FXCollections.observableArrayList(durations));
        if (!durations.isEmpty()) {
            durationChoice.getSelectionModel().selectFirst();
        }
        updateBookingAvailability();
    }

    private void clearSlots() {
        currentAvailability = null;
        startChoice.getItems().clear();
        durationChoice.getItems().clear();
        updateBookingAvailability();
    }

    private void updateBookingAvailability() {
        bookButton.setDisable(groupChoice.getValue() == null
                || !groupChoice.getValue().isActive()
                || itemsTable.getSelectionModel().getSelectedItem() == null
                || dateChoice.getValue() == null
                || startChoice.getValue() == null
                || durationChoice.getValue() == null);
    }

    @FXML
    private void handleBook(Event event) {
        OperatorGroupBean group = groupChoice.getValue();
        ItemBean item = itemsTable.getSelectionModel().getSelectedItem();
        try {
            BookingBean booking = logicController.createBooking(new BookingRequestBean(
                    group.getGroupId(), item.getItemId(), dateChoice.getValue(),
                    startChoice.getValue(), durationChoice.getValue()), operatorBean());
            showAlert(Alert.AlertType.INFORMATION, "Prenotazione confermata",
                    booking.getItemName() + " · " + DATE_FORMAT.format(booking.getDate())
                            + " · " + booking.getTimeRangeLabel());
            refreshStartSlots();
        } catch (RuntimeException e) {
            showError("Impossibile completare la prenotazione", e);
            refreshStartSlots();
        }
    }

    @FXML
    private void handleBackToDashboard(Event event) {
        navigator.showDashboard(operatorBean());
    }

    @FXML
    private void handleJoinGroup(Event event) {
        navigator.showRequestGroupAccess(operatorUsername);
    }

    @FXML
    private void handleBookings(Event event) {
        showAlert(Alert.AlertType.INFORMATION, "Le mie prenotazioni",
                "La consultazione delle prenotazioni sarà realizzata nel relativo caso d'uso.");
    }

    @FXML
    private void handleLogout(Event event) {
        navigator.showLogin();
    }

    private UserBean operatorBean() {
        return new UserBean(operatorUsername, Role.OPERATOR);
    }

    private ListCell<OperatorGroupBean> createGroupCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(OperatorGroupBean group, boolean empty) {
                super.updateItem(group, empty);
                boolean blocked = !empty && group != null && !group.isActive();
                setText(empty ? "" : formatGroup(group));
                setDisable(blocked);
                setStyle(blocked ? "-fx-opacity: 0.55; -fx-text-fill: #64748b;" : "");
            }
        };
    }

    private String formatGroup(OperatorGroupBean group) {
        if (group == null) {
            return "";
        }
        return group.getGroupName() + "  (" + TIME_FORMAT.format(group.getOpenTime())
                + " - " + TIME_FORMAT.format(group.getCloseTime()) + ") · "
                + group.getStatusLabel();
    }

    private void showError(String title, RuntimeException error) {
        String message = error.getMessage() == null ? "Si è verificato un errore." : error.getMessage();
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
