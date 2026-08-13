package boundary.cli;

import controller.ControllerAssembler;

import java.util.Scanner;

/** Dashboard CLI dell'amministratore autenticato. */
public class AdminDashboardBoundaryCLI {
    private final ControllerAssembler controllerAssembler;
    private final Scanner scanner;
    private final CliInput input;
    private final NotificationBoundaryCLI notifications;

    public AdminDashboardBoundaryCLI(ControllerAssembler controllerAssembler, Scanner scanner) {
        this.controllerAssembler = controllerAssembler;
        this.scanner = scanner;
        this.input = new CliInput(scanner);
        this.notifications = new NotificationBoundaryCLI(
                controllerAssembler.createAccessNotificationController(),
                controllerAssembler.createEventNotificationController());
    }

    public void start() {
        notifications.showForAdmin();
        while (true) {
            System.out.println("\n--- DASHBOARD ADMIN ---");
            System.out.println("1. Crea gruppo");
            System.out.println("2. Gestisci gruppi");
            System.out.println("3. Aggiorna notifiche");
            System.out.println("0. Logout");

            switch (input.readChoice("Scelta: ", 0, 3)) {
                case 1 -> new CreateGroupBoundaryCLI(
                        controllerAssembler.createCreateGroupController(), scanner).start();
                case 2 -> new ManageGroupBoundaryCLI(
                        controllerAssembler.createManageGroupController(), controllerAssembler,
                        scanner).showMenu();
                case 3 -> notifications.showForAdmin();
                case 0 -> {
                    return;
                }
                default -> throw new IllegalStateException("Scelta Admin non prevista.");
            }
        }
    }
}
