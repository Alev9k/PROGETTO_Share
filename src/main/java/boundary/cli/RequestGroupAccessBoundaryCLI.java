package boundary.cli;

import controller.JoinGroupController;
import model.bean.MembershipRequestBean;

import java.util.List;
import java.util.Scanner;

/** Boundary CLI del caso d'uso "Accedi a un gruppo". */
public class RequestGroupAccessBoundaryCLI {
    private final JoinGroupController controller;
    private final CliInput input;

    public RequestGroupAccessBoundaryCLI(JoinGroupController controller, Scanner scanner) {
        this.controller = controller;
        this.input = new CliInput(scanner);
    }

    public void start() {
        while (true) {
            showHistory();
            System.out.println("\n1. Invia una nuova richiesta");
            System.out.println("0. Torna alla dashboard");
            if (input.readChoice("Scelta: ", 0, 1) == 0) {
                return;
            }

            String token = input.readRequired("Token numerico di 6 cifre: ");
            try {
                MembershipRequestBean request = controller.requestAccess(token);
                System.out.println("Richiesta per '" + request.getGroupName()
                        + "' inviata. Stato: " + request.getStatusLabel() + ".");
            } catch (RuntimeException e) {
                System.err.println("Impossibile inviare la richiesta: " + e.getMessage());
            }
        }
    }

    private void showHistory() {
        try {
            List<MembershipRequestBean> requests = controller.getRequestHistory();
            System.out.println("\n--- LE MIE RICHIESTE DI ACCESSO ---");
            if (requests.isEmpty()) {
                System.out.println("Non hai ancora inviato richieste.");
                return;
            }
            for (MembershipRequestBean request : requests) {
                System.out.println(request.getGroupName() + " | "
                        + request.getStatusLabel() + " | " + request.getCreatedAtLabel());
            }
        } catch (RuntimeException e) {
            System.err.println("Impossibile caricare le richieste: " + e.getMessage());
        }
    }
}
