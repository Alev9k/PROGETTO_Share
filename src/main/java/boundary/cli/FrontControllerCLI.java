package boundary.cli;

import controller.ControllerAssembler;
import controller.LoginController;
import controller.RegistrationController;
import model.bean.UserBean;
import model.session.UserSession;

import java.util.Scanner;

/** Front controller della variante CLI. */
public class FrontControllerCLI {
    private final ControllerAssembler controllerAssembler;
    private final UserSession userSession;

    public FrontControllerCLI(ControllerAssembler controllerAssembler, UserSession userSession) {
        this.controllerAssembler = controllerAssembler;
        this.userSession = userSession;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        CliInput input = new CliInput(scanner);
        LoginController loginController = controllerAssembler.createLoginController();
        RegistrationController registrationController = controllerAssembler.createRegistrationController();

        while (true) {
            System.out.println("\n--- BENVENUTO IN SHARE ---");
            System.out.println("1. Registrati");
            System.out.println("2. Accedi");
            System.out.println("0. Esci");

            switch (input.readChoice("Scelta: ", 0, 2)) {
                case 1 -> new RegistrationBoundaryCLI(
                        registrationController, scanner).start();
                case 2 -> {
                    if (new LoginBoundaryCLI(loginController, scanner).start()) {
                        try {
                            dispatchAuthenticatedUser(scanner);
                        } finally {
                            userSession.close();
                        }
                    }
                }
                case 0 -> {
                    System.out.println("Arrivederci!");
                    return;
                }
                default -> throw new IllegalStateException("Scelta iniziale non prevista.");
            }
        }
    }

    private void dispatchAuthenticatedUser(Scanner scanner) {
        UserBean user = userSession.requireCurrentUser();
        System.out.println("\nAccesso effettuato come: " + user.getUsername());
        switch (user.getRole()) {
            case ADMIN -> new AdminDashboardBoundaryCLI(controllerAssembler, scanner).start();
            case OPERATOR -> new OperatorDashboardBoundaryCLI(controllerAssembler, scanner).start();
            case TECHNICIAN ->
                    System.out.println("[MENU TECNICO] Funzionalità non ancora disponibili.");
        }
    }
}
