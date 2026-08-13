package boundary.javafx;

import boundary.javafx.navigation.JavaFxSceneNavigator;
import controller.ControllerFactory;
import javafx.application.Application;
import javafx.stage.Stage;
import model.session.UserSession;

import java.util.Objects;

/** Entry point JavaFX: crea il navigatore d'istanza e gli affida la prima route. */
public class MainAppGUI extends Application {
    private static ControllerFactory controllerFactory;

    public static void setControllerFactory(ControllerFactory factory) {
        controllerFactory = Objects.requireNonNull(factory);
    }

    @Override
    public void start(Stage stage) {
        ControllerFactory factory = Objects.requireNonNull(
                controllerFactory,
                "ControllerFactory non configurata prima dell'avvio JavaFX."
        );
        new JavaFxSceneNavigator(stage, factory, UserSession.getInstance()).showLogin();
    }
}
