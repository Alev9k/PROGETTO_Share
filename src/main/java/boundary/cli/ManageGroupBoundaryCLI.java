package boundary.cli;

import controller.*;
import model.bean.GroupBean;
import model.bean.Role;
import model.bean.UserBean;

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
                    System.out.println("\n[!] Non ci sono gruppi associati al tuo account.");
                    System.out.println("Premi INVIO per tornare alla Dashboard e crearne uno nuovo...");
                    scanner.nextLine();
                    return; // Torna al menu precedente (la Dashboard)
                }

                System.out.println("\n--- GESTIONE GRUPPI ---");
                for (int i = 0; i < groups.size(); i++) {
                    System.out.println((i + 1) + ". " + groups.get(i).getGroupName());
                }
                System.out.println("0. Torna alla Dashboard"); // Meglio "Dashboard" che "Login"
                System.out.print("Scegli un gruppo o 0: ");

                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) break; // Esce dal while e torna al chiamante

                // Selezione e bivio decisionale
                if (choice > 0 && choice <= groups.size()) {
                    handleSubMenu(groups.get(choice - 1));
                } else {
                    System.out.println("[!] Scelta non valida.");
                }

            } catch (NumberFormatException e) {
                System.out.println("[!] Inserisci un numero valido.");
            } catch (Exception e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }
    }

    private void handleSubMenu(GroupBean group) {
        System.out.println("\nGruppo selezionato: " + group.getGroupName());
        System.out.println("1. Gestisci Membri\n2. Gestisci Beni\n0. Indietro");
        System.out.print("Scelta: ");

        try {
            int action = Integer.parseInt(scanner.nextLine());
            switch (action) {
                case 1 -> {
                    ManageOperatorsController moc = factory.createManageOperatorsController(group.getGroupId());
                    new ManageOperatorsBoundaryCLI(moc, group, scanner).start();
                }
                case 2 -> {
                    ManageItemsController mic = factory.createManageItemsController(group.getGroupId());
                    UserBean adminBean = new UserBean(adminUsername, Role.ADMIN);
                    new ManageItemsBoundaryCLI(mic, adminBean, scanner).start();
                }
                case 0 -> System.out.println("Ritorno alla lista gruppi...");
                default -> System.out.println("[!] Scelta non valida.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Inserisci un numero valido.");
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
