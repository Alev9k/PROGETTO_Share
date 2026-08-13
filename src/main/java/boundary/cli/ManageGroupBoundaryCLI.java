package boundary.cli;

import controller.ControllerFactory;
import controller.ManageGroupController;
import model.bean.GroupBean;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/** Boundary CLI per la selezione e gestione dei gruppi dell'Admin. */
public class ManageGroupBoundaryCLI {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ManageGroupController controller;
    private final ControllerFactory factory;
    private final Scanner scanner;
    private final CliInput input;

    public ManageGroupBoundaryCLI(ManageGroupController controller,
                                  ControllerFactory factory, Scanner scanner) {
        this.controller = controller;
        this.factory = factory;
        this.scanner = scanner;
        this.input = new CliInput(scanner);
    }

    public void showMenu() {
        while (true) {
            List<GroupBean> groups;
            try {
                groups = controller.getGroupList();
            } catch (RuntimeException e) {
                System.err.println("Impossibile recuperare i gruppi: " + e.getMessage());
                return;
            }
            System.out.println("\n--- GESTIONE GRUPPI ---");
            if (groups.isEmpty()) {
                System.out.println("Non ci sono gruppi associati al tuo account.");
                return;
            }
            for (int i = 0; i < groups.size(); i++) {
                GroupBean group = groups.get(i);
                System.out.println((i + 1) + ". " + group.getGroupName()
                        + " | token " + group.getAccessToken()
                        + " | " + TIME_FORMAT.format(group.getOpenTime()) + "-"
                        + TIME_FORMAT.format(group.getCloseTime()));
            }
            System.out.println("0. Torna alla dashboard");
            int choice = input.readChoice("Seleziona un gruppo: ", 0, groups.size());
            if (choice == 0) {
                return;
            }
            showGroupMenu(groups.get(choice - 1));
        }
    }

    private void showGroupMenu(GroupBean group) {
        while (true) {
            System.out.println("\n--- " + group.getGroupName().toUpperCase() + " ---");
            System.out.println("1. Gestisci membri e richieste");
            System.out.println("2. Gestisci item");
            System.out.println("0. Indietro");
            switch (input.readChoice("Scelta: ", 0, 2)) {
                case 1 -> new ManageOperatorsBoundaryCLI(
                        factory.createManageOperatorsController(group.getGroupId()),
                        group, scanner).start();
                case 2 -> new ManageItemsBoundaryCLI(
                        factory.createManageItemsController(group.getGroupId()),
                        scanner).start();
                case 0 -> {
                    return;
                }
                default -> throw new IllegalStateException("Scelta gruppo non prevista.");
            }
        }
    }
}
