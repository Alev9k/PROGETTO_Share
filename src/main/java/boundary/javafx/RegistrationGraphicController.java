package boundary.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import controller.RegistrationController;
import controller.ControllerFactory; // Importiamo la nostra Factory
import exceptions.UserAlreadyExistsException;

public class RegistrationGraphicController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;

    private RegistrationController registrationController;
    private ControllerFactory factory; // Sostituiamo UserDAO con la Factory

    @FXML
    public void initialize() {
        // Inizializziamo le opzioni del ruolo
        roleChoiceBox.getItems().addAll("Admin", "Operator", "Technician");
        roleChoiceBox.setValue("Operator");
    }

    /**
     * Nuovo metodo di iniezione che segue il pattern Factory.
     */
    public void setFactory(ControllerFactory factory) {
        this.factory = factory;
        // Chiediamo alla factory di crearci il controller logico per la registrazione
        this.registrationController = factory.createRegistrationController();
    }

    @FXML
    private void handleRegistration(ActionEvent event) {
        String u = usernameField.getText();
        String p = passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Dati Mancanti", "Inserisci tutti i campi prima di procedere.");
            return;
        }

        // Mappiamo la scelta (1=Admin, 2=Operator, 3=Technician)
        int type = roleChoiceBox.getSelectionModel().getSelectedIndex() + 1;

        try {
            registrationController.register(u, p, type);
            showInfo("Registrazione OK", "Account creato con successo! Torna al login per accedere.");
            backToLogin();
        } catch (UserAlreadyExistsException e) {
            showError("Errore", e.getMessage());
        } catch (Exception e) {
            showError("Errore Sistema", "Si è verificato un problema durante la registrazione.");
        }
    }

    @FXML
    private void backToLogin() {
        try {
            MainAppGUI.showLogin();
        } catch (Exception e) {
            showError("Errore", "Ritorno al login fallito.");
        }
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
