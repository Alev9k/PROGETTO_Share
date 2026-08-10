package boundary.javafx;

import boundary.javafx.navigation.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import controller.LoginController;
import exceptions.InvalidCredentialsException;
import model.bean.*;

public class LoginGraphicController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private LoginController loginController;
    private SceneNavigator navigator;

    /** Inietta il controller del caso d'uso e il contratto di navigazione. */
    public void initData(LoginController loginController, SceneNavigator navigator) {
        this.loginController = loginController;
        this.navigator = navigator;
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
            navigator.showDashboard(loggedUser);

        } catch (InvalidCredentialsException e) {
            showError("Errore Login", e.getMessage());
        } catch (Exception e) {
            showError("Errore Sistema", "Si è verificato un errore imprevisto.");
        }
    }

    @FXML
    private void goToRegistration() {
        try {
            navigator.showRegistration();
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
