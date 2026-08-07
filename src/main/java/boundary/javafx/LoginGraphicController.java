package boundary.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import controller.LoginController;
import controller.ControllerFactory; // Nuova importazione
import exceptions.InvalidCredentialsException;
import model.bean.*;
import java.io.IOException;

public class LoginGraphicController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private LoginController loginController;
    private ControllerFactory factory; // Sostituiamo il DAO con la Factory

    /**
     * Metodo per iniettare la Factory.
     * È il cuore del disaccoppiamento: la GUI non sa nulla di database o file.
     */
    public void setFactory(ControllerFactory factory) {
        this.factory = factory;
        // Chiediamo alla factory di crearci il controller logico per il login
        this.loginController = factory.createLoginController();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String u = usernameField.getText();
        String p = passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Campi Vuoti", "Per favore, inserisci username e password.");
            return;
        }

        try {
            UserBean loggedUser = loginController.login(u, p);
            showInfo("Accesso Eseguito", "Benvenuto " + loggedUser.getUsername() + "!");
            MainAppGUI.showDashboard(loggedUser);

        } catch (InvalidCredentialsException e) {
            showError("Errore Login", e.getMessage());
        } catch (Exception e) {
            showError("Errore Sistema", "Si è verificato un errore imprevisto.");
        }
    }

    @FXML
    private void goToRegistration() {
        try {
            MainAppGUI.replaceScene("/view/Registration.fxml",
                    (RegistrationGraphicController controller) -> controller.setFactory(factory));
        } catch (Exception e) {
            showError("Errore Sistema", "Impossibile caricare la pagina di registrazione.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
