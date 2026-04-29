package boundary.javaFX;

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
import model.dao.UserDAO;
import exceptions.UserAlreadyExistsException;

import java.io.IOException;

public class RegistrationGraphicController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;

    private RegistrationController registrationController;
    private UserDAO userDAO;

    @FXML
    public void initialize() {
        // Inizializziamo le opzioni del ruolo (stile Frutiger Aero/Windows 7)
        roleChoiceBox.getItems().addAll("Admin", "Operator", "Technician");
        roleChoiceBox.setValue("Operator");
    }

    public void setDAO(UserDAO dao) {
        this.userDAO = dao;
        this.registrationController = new RegistrationController(dao);
    }

    @FXML
    private void handleRegistration(ActionEvent event) {
        String u = usernameField.getText();
        String p = passwordField.getText();
        // Mappiamo la scelta (1, 2 o 3) in base all'ordine nel ChoiceBox
        int type = roleChoiceBox.getSelectionModel().getSelectedIndex() + 1;

        try {
            registrationController.register(u, p, type);
            showInfo("Registrazione OK", "Account creato con successo! Torna al login per accedere.");
            backToLogin(event);
        } catch (UserAlreadyExistsException e) {
            showError("Errore", e.getMessage());
        }
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/share/view/login-view.fxml"));
            Parent root = loader.load();

            LoginGraphicController loginCtrl = loader.getController();
            loginCtrl.setDAO(userDAO);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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