package boundary.cli;

import controller.ControllerFactory;

import java.util.Scanner;

/** Dashboard CLI dell'amministratore autenticato. */
public class AdminDashboardBoundaryCLI {
    private final ControllerFactory factory;
    private final Scanner scanner;
    private final CliInput input;
    private final NotificationBoundaryCLI notifications;

    public AdminDashboardBoundaryCLI(ControllerFactory factory, Scanner scanner) {
        this.factory = factory;
        this.scanner = scanner;
        this.input = new CliInput(scanner);
        this.notifications = new NotificationBoundaryCLI(
                factory.createAccessNotificationController(),
                factory.createEventNotificationController());
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
                        factory.createCreateGroupController(), scanner).start();
                case 2 -> new ManageGroupBoundaryCLI(
                        factory.createManageGroupController(), factory, scanner).showMenu();
                case 3 -> notifications.showForAdmin();
                case 0 -> {
                    return;
                }
                default -> throw new IllegalStateException("Scelta Admin non prevista.");
            }
        }
    }
}
