package boundary.cli;

import controller.ManageItemsController;
import model.bean.CreateItemBean;
import model.bean.ItemBean;
import model.bean.UserBean;

import java.util.List;
import java.util.Scanner;

/** Boundary CLI del caso d'uso di visualizzazione e creazione degli item. */
public class ManageItemsBoundaryCLI {
    private final ManageItemsController controller;
    private final UserBean adminBean;
    private final Scanner scanner;

    public ManageItemsBoundaryCLI(ManageItemsController controller, UserBean adminBean,
                                  Scanner scanner) {
        this.controller = controller;
        this.adminBean = adminBean;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n--- GESTIONE BENI DEL GRUPPO ---");
            System.out.println("1. Visualizza tutti i beni");
            System.out.println("2. Aggiungi un nuovo bene");
            System.out.println("0. Torna al menu gruppo");
            System.out.print("Scelta: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) {
                    return;
                }

                switch (choice) {
                    case 1 -> showItems();
                    case 2 -> createItem();
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Per favore, inserisci un numero valido.");
            }
        }
    }

    private void showItems() {
        try {
            List<ItemBean> items = controller.getItemList(adminBean);
            if (items.isEmpty()) {
                System.out.println("Non ci sono beni in questo gruppo.");
                return;
            }

            System.out.println("\nElenco beni:");
            for (int i = 0; i < items.size(); i++) {
                ItemBean item = items.get(i);
                System.out.println((i + 1) + ". " + item.getItemName()
                        + " | priorità: " + item.getPriority()
                        + " | uso massimo: " + item.getMaxUsageTime() + " minuti");
            }
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }

    private void createItem() {
        try {
            System.out.print("Inserisci il nome del nuovo bene: ");
            String name = scanner.nextLine();
            System.out.print("Inserisci la priorità (1-5): ");
            int priority = Integer.parseInt(scanner.nextLine());
            System.out.print("Inserisci il tempo massimo di utilizzo in minuti: ");
            int maxUsageTime = Integer.parseInt(scanner.nextLine());

            controller.createItem(
                    new CreateItemBean(name, priority, maxUsageTime), adminBean);
            System.out.println("Bene aggiunto con successo!");
        } catch (NumberFormatException e) {
            System.err.println("Priorità e tempo massimo devono essere numeri interi.");
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
