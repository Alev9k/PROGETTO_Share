package boundary.cli;

import controller.CreateGroupController;
import model.bean.GroupBean;

import java.time.LocalTime;
import java.util.Scanner;

/** Boundary CLI del caso d'uso "Crea gruppo". */
public class CreateGroupBoundaryCLI {
    private final CreateGroupController controller;
    private final CliInput input;

    public CreateGroupBoundaryCLI(CreateGroupController controller, Scanner scanner) {
        this.controller = controller;
        this.input = new CliInput(scanner);
    }

    public void start() {
        System.out.println("\n--- CREA GRUPPO ---");
        String name = input.readRequired("Nome del gruppo: ");
        LocalTime openTime = input.readTime("Orario di apertura");
        LocalTime closeTime = input.readTime("Orario di chiusura");

        try {
            GroupBean created = controller.createGroup(
                    new GroupBean(name, openTime, closeTime));
            System.out.println("Gruppo creato correttamente.");
            System.out.println("Token di accesso: " + created.getAccessToken());
            System.out.println("Comunica il token agli operatori che vuoi invitare.");
        } catch (Exception e) {
            System.err.println("Impossibile creare il gruppo: " + e.getMessage());
        }
    }
}
