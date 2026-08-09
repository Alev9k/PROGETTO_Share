package boundary.javafx;

import controller.ControllerFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import model.bean.UserBean;

import java.io.IOException;

public class MainAppGUI extends Application {
    private static Stage primaryStage; // Riferimento statico per il cambio scene
    private static ControllerFactory controllerFactory;

    public static void setControllerFactory(ControllerFactory factory) {
        controllerFactory = factory;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLogin();
    }

    public static void showLogin() throws Exception {
        // 1. Blocchiamo il ridimensionamento PRIMA di caricare la scena
        primaryStage.setResizable(false);

        replaceScene("/view/Login.fxml", (LoginGraphicController ctrl) -> ctrl.setFactory(controllerFactory));

        // 2. Opzionale: Forza la finestra a adattarsi perfettamente alle dimensioni del nuovo FXML
        primaryStage.sizeToScene();
    }

    // IL DISPATCHER CENTRALE
    public static void showDashboard(UserBean user) {
        try {
            primaryStage.setResizable(true);
            switch (user.getRole()) {
                case ADMIN -> replaceScene("/view/AdminDashboard.fxml",
                        (AdminDashboardGraphicController ctrl) -> ctrl.initData(controllerFactory, user.getUsername()));

                case OPERATOR -> replaceScene("/view/OperatorDashboard.fxml",
                        (OperatorDashboardGraphicController ctrl) ->
                                ctrl.initData(controllerFactory, user.getUsername()));

                case TECHNICIAN -> replaceScene("/view/TechnicianDashboard.fxml",
                        (TechnicianDashboardGraphicController ctrl) -> System.out.println("Init Tech..."));
            }
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo generico per caricare scene ed iniettare dati
    public static <T> void replaceScene(String fxmlPath, java.util.function.Consumer<T> controllerInit) {
        try {
            FXMLLoader loader = new FXMLLoader(MainAppGUI.class.getResource(fxmlPath));
            Parent root = loader.load();

            T controller = loader.getController();
            if (controllerInit != null) {
                controllerInit.accept(controller);
            }

            if (primaryStage.getScene() == null) {
                // Se è la prima volta (es. al login), creiamo la Scena da zero
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                // Se esiste già, cambiamo solo il contenuto (più veloce e fluido)
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Errore fatale: Impossibile caricare FXML in " + fxmlPath);
            e.printStackTrace();
        }
    }
}
