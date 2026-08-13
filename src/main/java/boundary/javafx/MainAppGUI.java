package boundary.javafx;

import boundary.javafx.navigation.JavaFxSceneNavigator;
import controller.ControllerAssembler;
import javafx.application.Application;
import javafx.stage.Stage;
import model.session.UserSession;

import java.util.Objects;

/** Entry point JavaFX: crea il navigatore d'istanza e gli affida la prima route. */
public class MainAppGUI extends Application {
    private static ControllerAssembler controllerAssembler;

    public static void setControllerAssembler(ControllerAssembler assembler) {
        controllerAssembler = Objects.requireNonNull(assembler);
    }

    @Override
    public void start(Stage stage) {
        ControllerAssembler assembler = Objects.requireNonNull(
                controllerAssembler,
                "ControllerAssembler non configurato prima dell'avvio JavaFX."
        );
        new JavaFxSceneNavigator(stage, assembler, UserSession.getInstance()).showLogin();
    }
}
