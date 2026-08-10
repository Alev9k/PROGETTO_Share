package boundary.javafx.navigation;

import boundary.javafx.AdminDashboardGraphicController;
import boundary.javafx.CreateGroupGraphicController;
import boundary.javafx.LoginGraphicController;
import boundary.javafx.ManageGroupGraphicController;
import boundary.javafx.ManageItemsGraphicController;
import boundary.javafx.ManageOperatorsGraphicController;
import boundary.javafx.OperatorDashboardGraphicController;
import boundary.javafx.RegistrationGraphicController;
import boundary.javafx.RequestGroupAccessGraphicController;
import controller.ControllerFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.bean.GroupBean;
import model.bean.UserBean;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/** Implementazione JavaFX che possiede lo Stage e centralizza tutte le route. */
public final class JavaFxSceneNavigator implements SceneNavigator {
    private final Stage stage;
    private final ControllerFactory controllerFactory;

    public JavaFxSceneNavigator(Stage stage, ControllerFactory controllerFactory) {
        this.stage = Objects.requireNonNull(stage);
        this.controllerFactory = Objects.requireNonNull(controllerFactory);
    }

    @Override
    public void showLogin() {
        stage.setResizable(false);
        loadScene("/view/Login.fxml", (LoginGraphicController controller) ->
                controller.initData(controllerFactory.createLoginController(), this));
    }

    @Override
    public void showRegistration() {
        loadScene("/view/Registration.fxml", (RegistrationGraphicController controller) ->
                controller.initData(controllerFactory.createRegistrationController(), this));
    }

    @Override
    public void showDashboard(UserBean user) {
        Objects.requireNonNull(user, "L'utente autenticato è obbligatorio.");
        stage.setResizable(true);

        switch (user.getRole()) {
            case ADMIN -> loadScene("/view/AdminDashboard.fxml",
                    (AdminDashboardGraphicController controller) -> controller.initData(
                            controllerFactory.createAccessNotificationController(),
                            this,
                            user.getUsername()));
            case OPERATOR -> loadScene("/view/OperatorDashboard.fxml",
                    (OperatorDashboardGraphicController controller) -> controller.initData(
                            controllerFactory.createAccessNotificationController(),
                            this,
                            user.getUsername()));
            case TECHNICIAN -> loadScene("/view/TechnicianDashboard.fxml", null);
        }
    }

    @Override
    public void showCreateGroup(String adminUsername) {
        loadScene("/view/CreateGroup.fxml", (CreateGroupGraphicController controller) ->
                controller.initData(
                        controllerFactory.createCreateGroupController(), this, adminUsername));
    }

    @Override
    public void showManageGroups(String adminUsername) {
        loadScene("/view/ManageGroup.fxml", (ManageGroupGraphicController controller) ->
                controller.initData(
                        controllerFactory.createManageGroupController(), this, adminUsername));
    }

    @Override
    public void showManageItems(GroupBean group, String adminUsername) {
        Objects.requireNonNull(group, "Il gruppo da gestire è obbligatorio.");
        loadScene("/view/ManageItems.fxml", (ManageItemsGraphicController controller) ->
                controller.initData(
                        controllerFactory.createManageItemsController(group.getGroupId()),
                        this,
                        group,
                        adminUsername));
    }

    @Override
    public void showManageOperators(GroupBean group, String adminUsername) {
        Objects.requireNonNull(group, "Il gruppo da gestire è obbligatorio.");
        loadScene("/view/ManageOperators.fxml", (ManageOperatorsGraphicController controller) ->
                controller.initData(
                        controllerFactory.createManageOperatorsController(group.getGroupId()),
                        this,
                        group,
                        adminUsername));
    }

    @Override
    public void showRequestGroupAccess(String operatorUsername) {
        loadScene("/view/RequestGroupAccess.fxml",
                (RequestGroupAccessGraphicController controller) -> controller.initData(
                        controllerFactory.createJoinGroupController(), this, operatorUsername));
    }

    private <T> void loadScene(String fxmlPath, Consumer<T> controllerInitializer) {
        try {
            FXMLLoader loader = new FXMLLoader(JavaFxSceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            if (controllerInitializer != null) {
                T controller = loader.getController();
                controllerInitializer.accept(controller);
            }

            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }

            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | RuntimeException e) {
            throw new NavigationException("Impossibile caricare la schermata " + fxmlPath, e);
        }
    }
}
