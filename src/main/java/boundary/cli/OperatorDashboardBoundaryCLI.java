package boundary.cli;

import controller.ControllerAssembler;

import java.util.Scanner;

/** Dashboard CLI dell'operatore autenticato. */
public class OperatorDashboardBoundaryCLI {
    private final ControllerAssembler controllerAssembler;
    private final Scanner scanner;
    private final CliInput input;
    private final NotificationBoundaryCLI notifications;

    public OperatorDashboardBoundaryCLI(ControllerAssembler controllerAssembler, Scanner scanner) {
        this.controllerAssembler = controllerAssembler;
        this.scanner = scanner;
        this.input = new CliInput(scanner);
        this.notifications = new NotificationBoundaryCLI(
                controllerAssembler.createAccessNotificationController(),
                controllerAssembler.createEventNotificationController());
    }

    public void start() {
        notifications.showForOperator();
        while (true) {
            System.out.println("\n--- DASHBOARD OPERATOR ---");
            System.out.println("1. I miei gruppi e prenota item");
            System.out.println("2. Accedi a un gruppo");
            System.out.println("3. Le mie prenotazioni");
            System.out.println("4. Aggiorna notifiche");
            System.out.println("0. Logout");

            switch (input.readChoice("Scelta: ", 0, 4)) {
                case 1 -> new BookItemBoundaryCLI(
                        controllerAssembler.createBookItemController(), scanner).start();
                case 2 -> new RequestGroupAccessBoundaryCLI(
                        controllerAssembler.createJoinGroupController(), scanner).start();
                case 3 -> new MyBookingsBoundaryCLI(
                        controllerAssembler.createMyBookingsController(),
                        controllerAssembler.createReturnItemController(), scanner).start();
                case 4 -> notifications.showForOperator();
                case 0 -> {
                    return;
                }
                default -> throw new IllegalStateException("Scelta Operator non prevista.");
            }
        }
    }
}
