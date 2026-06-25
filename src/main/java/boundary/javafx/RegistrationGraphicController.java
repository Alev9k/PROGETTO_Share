package boundary.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import controller.RegistrationController;
import controller.ControllerFactory; // Importiamo la nostra Factory
import exceptions.UserAlreadyExistsException;

import java.io.IOException;

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
            backToLogin(event);
        } catch (UserAlreadyExistsException e) {
            showError("Errore", e.getMessage());
        } catch (Exception e) {
            showError("Errore Sistema", "Si è verificato un problema durante la registrazione.");
        }
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();

            // Passiamo la factory al controller del login per mantenere il flusso
            LoginGraphicController loginCtrl = loader.getController();
            loginCtrl.setFactory(factory);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Applichiamo la protezione per il layout anche qui
            stage.setResizable(false);

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
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