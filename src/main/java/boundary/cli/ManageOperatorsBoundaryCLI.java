package boundary.cli;

import model.bean.*;
import controller.ManageOperatorsController;
import java.util.List;
import java.util.Scanner;

public class ManageOperatorsBoundaryCLI {
    private final GroupBean group;
    private final Scanner scanner;
    private final ManageOperatorsController controller;

    public ManageOperatorsBoundaryCLI(ManageOperatorsController moc, GroupBean group, Scanner scanner) {
        this.group = group;
        this.scanner = scanner;
        this.controller = moc;
    }

    public void start() {
        try {
            // Step 5: Mostra membri del gruppo
            List<OperatorBean> ops = controller.getOperatorList();

            System.out.println("\n--- MEMBRI DEL GRUPPO: " + group.getGroupName() + " ---");
            for (int i = 0; i < ops.size(); i++) {
                String stato = (ops.get(i).getStatus() == 0) ? "ATTIVO" : "BLOCCATO";
                System.out.println((i + 1) + ". " + ops.get(i).getUsername() + " [" + stato + "]");
            }

            System.out.print("Scegli un operatore per cambiare stato (0 per annullare): ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice > 0) {
                // Step 10: Toggle block
                controller.toggleBlock(ops.get(choice - 1));
                System.out.println("Stato aggiornato con successo (Step 11: Notifica inviata).");
            }
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage()); // Gestisce 9c: Operatore ha un bene
        }
    }
}