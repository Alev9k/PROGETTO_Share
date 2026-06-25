package boundary.cli;

import controller.*;
import model.bean.GroupBean;
import java.util.List;
import java.util.Scanner;

public class ManageGroupBoundaryCLI {
    private final ManageGroupController controller;
    private final ControllerFactory factory;
    private final String adminUsername;
    private final Scanner scanner;

    public ManageGroupBoundaryCLI(ManageGroupController ctrl, ControllerFactory factory, String username, Scanner scanner) {
        this.controller = ctrl;
        this.factory = factory;
        this.adminUsername = username;
        this.scanner = scanner;
    }

    public void showMenu() {
        while (true) {
            try {
                // Step 1: Recupero dei gruppi (Bean)
                List<GroupBean> groups = controller.getGroupList(adminUsername);
                if (groups.isEmpty()) {
                    System.out.println("\n[!] Non ci sono gruppi creati.");
                    //chiede di creare un nuovo gruppo o torna al menu
                    return;
                }

                System.out.println("\n--- GESTIONE GRUPPI ---");
                for (int i = 0; i < groups.size(); i++) {
                    System.out.println((i + 1) + ". " + groups.get(i).getGroupName());
                }
                System.out.println("0. Torna al Login");
                System.out.print("Scegli un gruppo o 0: ");

                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) break;

                //Selezione e bivio decisionale
                handleSubMenu(groups.get(choice - 1));

            } catch (Exception e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }
    }

    private void handleSubMenu(GroupBean group) {
        System.out.println("\nGruppo selezionato: " + group.getGroupName());
        System.out.println("1. Gestisci Membri\n2. Gestisci Beni\n3. Elimina Gruppo");
        System.out.print("Scelta: ");

        try {
            int action = Integer.parseInt(scanner.nextLine());
            switch (action) {
                case 1 -> {
                    // Chiediamo alla factory di crearci il controller
                    ManageOperatorsController moc = factory.createManageOperatorsController(group.getGroupId());
                    new ManageOperatorsBoundaryCLI(moc, group, scanner).start();
                }
                case 2 -> {
                    // La boundary non sa come si costruisce il controller, lo riceve e basta
                    ManageItemsController mic = factory.createManageItemsController(group.getGroupId());
                    new ManageItemsBoundaryCLI(mic, scanner).start();
                }
                case 3 -> controller.deleteGroup(group.getGroupId());
            }
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }

    private void deleteGroup(int id) {
        try {
            controller.deleteGroup(id);
            System.out.println("Gruppo eliminato con successo!");
        } catch (Exception e) { System.err.println(e.getMessage()); }
    }
}