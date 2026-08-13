package boundary.javafx.navigation;

import boundary.javafx.AdminDashboardGraphicController;
import boundary.javafx.CreateGroupGraphicController;
import boundary.javafx.LoginGraphicController;
import boundary.javafx.ManageGroupGraphicController;
import boundary.javafx.ManageItemsGraphicController;
import boundary.javafx.ManageOperatorsGraphicController;
import boundary.javafx.MyGroupsGraphicController;
import boundary.javafx.MyBookingsGraphicController;
import boundary.javafx.OperatorDashboardGraphicController;
import boundary.javafx.RegistrationGraphicController;
import boundary.javafx.RequestGroupAccessGraphicController;
import controller.ControllerAssembler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.bean.GroupBean;
import model.bean.UserBean;
import model.session.UserSession;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/** Implementazione JavaFX che possiede lo Stage e centralizza tutte le route. */
public class JavaFxSceneNavigator implements SceneNavigator {
    private final Stage stage;
    private final ControllerAssembler controllerAssembler;
    private final UserSession userSession;

    public JavaFxSceneNavigator(Stage stage, ControllerAssembler controllerAssembler,
                                UserSession userSession) {
        this.stage = Objects.requireNonNull(stage);
        this.controllerAssembler = Objects.requireNonNull(controllerAssembler);
        this.userSession = Objects.requireNonNull(userSession);
    }

    @Override
    public void showLogin() {
        stage.setResizable(false);
        loadScene("/view/Login.fxml", (LoginGraphicController controller) ->
                controller.initData(controllerAssembler.createLoginController(), this));
    }

    @Override
    public void showRegistration() {
        loadScene("/view/Registration.fxml", (RegistrationGraphicController controller) ->
                controller.initData(controllerAssembler.createRegistrationController(), this));
    }

    @Override
    public void showDashboard() {
        UserBean user = userSession.requireCurrentUser();
        Objects.requireNonNull(user, "L'utente autenticato è obbligatorio.");
        stage.setResizable(true);

        switch (user.getRole()) {
            case ADMIN -> loadScene("/view/AdminDashboard.fxml",
                    (AdminDashboardGraphicController controller) -> controller.initData(
                            controllerAssembler.createAccessNotificationController(),
                            controllerAssembler.createEventNotificationController(),
                            this, userSession));
            case OPERATOR -> loadScene("/view/OperatorDashboard.fxml",
                    (OperatorDashboardGraphicController controller) -> controller.initData(
                            controllerAssembler.createAccessNotificationController(),
                            controllerAssembler.createEventNotificationController(),
                            this, userSession));
        }
    }

    @Override
    public void showCreateGroup() {
        loadScene("/view/CreateGroup.fxml", (CreateGroupGraphicController controller) ->
                controller.initData(
                        controllerAssembler.createCreateGroupController(), this));
    }

    @Override
    public void showManageGroups() {
        loadScene("/view/ManageGroup.fxml", (ManageGroupGraphicController controller) ->
                controller.initData(
                        controllerAssembler.createManageGroupController(), this));
    }

    @Override
    public void showManageItems(GroupBean group) {
        Objects.requireNonNull(group, "Il gruppo da gestire è obbligatorio.");
        loadScene("/view/ManageItems.fxml", (ManageItemsGraphicController controller) ->
                controller.initData(
                        controllerAssembler.createManageItemsController(group.getGroupId()),
                        this, group));
    }

    @Override
    public void showManageOperators(GroupBean group) {
        Objects.requireNonNull(group, "Il gruppo da gestire è obbligatorio.");
        loadScene("/view/ManageOperators.fxml", (ManageOperatorsGraphicController controller) ->
                controller.initData(
                        controllerAssembler.createManageOperatorsController(group.getGroupId()),
                        this, group));
    }

    @Override
    public void showRequestGroupAccess() {
        loadScene("/view/RequestGroupAccess.fxml",
                (RequestGroupAccessGraphicController controller) -> controller.initData(
                        controllerAssembler.createJoinGroupController(), this));
    }

    @Override
    public void showMyGroups() {
        loadScene("/view/MyGroups.fxml", (MyGroupsGraphicController controller) ->
                controller.initData(controllerAssembler.createBookItemController(), this));
    }

    @Override
    public void showMyBookings() {
        loadScene("/view/MyBookings.fxml", (MyBookingsGraphicController controller) ->
                controller.initData(controllerAssembler.createMyBookingsController(),
                        controllerAssembler.createReturnItemController(), this));
    }

    @Override
    public void logout() {
        userSession.close();
        showLogin();
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
