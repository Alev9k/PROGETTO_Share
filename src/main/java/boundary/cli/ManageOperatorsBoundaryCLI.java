package boundary.cli;

import controller.ManageOperatorsController;
import model.bean.GroupBean;
import model.bean.MembershipRequestBean;
import model.bean.OperatorBean;

import java.util.List;
import java.util.Scanner;

/** Boundary CLI per richieste di accesso e membri di un gruppo. */
public class ManageOperatorsBoundaryCLI {
    private final GroupBean group;
    private final ManageOperatorsController controller;
    private final CliInput input;
    private static final String ANNULLA = "0. Annulla";

    public ManageOperatorsBoundaryCLI(ManageOperatorsController controller,
                                      GroupBean group, Scanner scanner) {
        this.group = group;
        this.controller = controller;
        this.input = new CliInput(scanner);
    }

    public void start() {
        while (true) {
            System.out.println("\n--- MEMBRI DI " + group.getGroupName().toUpperCase() + " ---");
            System.out.println("1. Gestisci richieste pendenti");
            System.out.println("2. Blocca o riattiva un membro");
            System.out.println("0. Torna al gruppo");
            switch (input.readChoice("Scelta: ", 0, 2)) {
                case 1 -> managePendingRequests();
                case 2 -> manageMembers();
                case 0 -> {
                    return;
                }
                default -> throw new IllegalStateException("Scelta membri non prevista.");
            }
        }
    }

    private void managePendingRequests() {
        try {
            List<MembershipRequestBean> requests = controller.getPendingRequests();
            if (requests.isEmpty()) {
                System.out.println("Non ci sono richieste di accesso pendenti.");
                return;
            }
            for (int i = 0; i < requests.size(); i++) {
                MembershipRequestBean request = requests.get(i);
                System.out.println((i + 1) + ". " + request.getOperatorUsername()
                        + " | " + request.getCreatedAtLabel());
            }
            System.out.println(ANNULLA);
            int choice = input.readChoice("Seleziona una richiesta: ", 0, requests.size());
            if (choice == 0) {
                return;
            }
            MembershipRequestBean selected = requests.get(choice - 1);
            System.out.println("1. Accetta");
            System.out.println("2. Rifiuta");
            System.out.println(ANNULLA);
            switch (input.readChoice("Decisione: ", 0, 2)) {
                case 1 -> {
                    controller.acceptRequest(selected);
                    System.out.println(selected.getOperatorUsername()
                            + " è ora membro del gruppo.");
                }
                case 2 -> {
                    controller.rejectRequest(selected);
                    System.out.println("Richiesta rifiutata.");
                }
                case 0 -> { }
                default -> throw new IllegalStateException("Decisione non prevista.");
            }
        } catch (Exception e) {
            System.err.println("Impossibile gestire la richiesta: " + e.getMessage());
        }
    }

    private void manageMembers() {
        try {
            List<OperatorBean> members = controller.getOperatorList();
            if (members.isEmpty()) {
                System.out.println("Il gruppo non contiene ancora operatori.");
                return;
            }
            for (int i = 0; i < members.size(); i++) {
                OperatorBean member = members.get(i);
                System.out.println((i + 1) + ". " + member.getUsername()
                        + " | " + member.getStatusLabel());
            }
            System.out.println(ANNULLA);
            int choice = input.readChoice("Seleziona un membro: ", 0, members.size());
            if (choice == 0) {
                return;
            }
            OperatorBean selected = members.get(choice - 1);
            String action = selected.getStatus() == 0 ? "bloccare" : "riattivare";
            if (!input.confirm("Vuoi " + action + " " + selected.getUsername() + "?")) {
                return;
            }
            controller.toggleBlock(selected);
            System.out.println("Stato del membro aggiornato correttamente.");
        } catch (Exception e) {
            System.err.println("Impossibile aggiornare il membro: " + e.getMessage());
        }
    }
}
