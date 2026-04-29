package boundary.javaFX;

import model.dao.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class MainAppGUI extends Application {
    private static UserDAO selectedDAO; // Passato dal Main
    public static void setDAO(UserDAO dao) { selectedDAO = dao; }
    @Override
    public void start(Stage stage) throws Exception {
        // Carica il file FXML che abbiamo disegnato con Scene Builder
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login-view.fxml"));
        Parent root = loader.load();

        // TRUCCO: Prendiamo il controller appena creato e gli "iniettiamo" il DAO
        LoginGraphicController guiCtrl = loader.getController();
        guiCtrl.setDAO(selectedDAO);

        stage.setScene(new Scene(root));
        stage.show();
    }
}