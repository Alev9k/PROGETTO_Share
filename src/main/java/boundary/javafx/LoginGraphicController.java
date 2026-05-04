package boundary.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import controller.LoginController;
import model.dao.UserDAO;
import model.entity.User;
import exceptions.InvalidCredentialsException;

import java.io.IOException;

public class LoginGraphicController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private LoginController loginController;
    private UserDAO userDAO;

    // Metodo fondamentale per iniettare il DAO dal MainAppGUI
    public void setDAO(UserDAO dao) {
        this.userDAO = dao;
        this.loginController = new LoginController(dao);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String u = usernameField.getText();
        String p = passwordField.getText();

        try {
            User loggedUser = loginController.login(u, p);
            // Se arriviamo qui, il login ha avuto successo
            showInfo("Accesso Eseguito", "Benvenuto " + loggedUser.getUsername() + "!");
            // Qui caricheresti la Dashboard...

        } catch (InvalidCredentialsException e) {
            showError("Errore Login", e.getMessage());
        }
    }

    @FXML
    private void goToRegistration(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Registration.fxml"));
            Parent root = loader.load();

            // Passiamo il DAO anche al controller della registrazione!
            RegistrationGraphicController regCtrl = loader.getController();
            regCtrl.setDAO(userDAO);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
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