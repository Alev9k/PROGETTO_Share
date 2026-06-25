package boundary.cli;

import model.bean.ItemBean;
import controller.ManageItemsController;
import java.util.List;
import java.util.Scanner;

public class ManageItemsBoundaryCLI {
    private final ManageItemsController controller;
    private final Scanner scanner;

    public ManageItemsBoundaryCLI(ManageItemsController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n--- GESTIONE BENI DEL GRUPPO ---");
            System.out.println("1. Visualizza tutti i beni");
            System.out.println("2. Aggiungi un nuovo bene");
            System.out.println("3. Rimuovi un bene");
            System.out.println("0. Torna al menu gruppo");
            System.out.print("Scelta: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) break;

                switch (choice) {
                    case 1 -> showItems();
                    case 2 -> addNewItem();
                    case 3 -> removeItem();
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Per favore, inserisci un numero valido.");
            }
        }
    }

    private void showItems() {
        List<ItemBean> items = controller.getItemList();
        if (items.isEmpty()) {
            System.out.println("Non ci sono beni in questo gruppo.");
            return;
        }
        System.out.println("\nElenco beni:");
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i).getItemName() +
                    " (Tipo: " + items.get(i).getAssetName() + ")");
        }
    }

    private void addNewItem() {
        System.out.print("Inserisci il nome del nuovo bene: ");
        String name = scanner.nextLine();
        System.out.print("Inserisci la tipologia (Asset): ");
        String assetName = scanner.nextLine();

        try {
            ItemBean newBean = new ItemBean(name, assetName);
            controller.addNewItem(newBean);
            System.out.println("Bene aggiunto con successo!");
        } catch (Exception e) {
            // Gestisce DuplicateItemNameException (Step 11ab)
            System.err.println("Errore: " + e.getMessage());
        }
    }

    private void removeItem() {
        List<ItemBean> items = controller.getItemList();
        if (items.isEmpty()) {
            System.out.println("Nessun bene da rimuovere.");
            return;
        }

        showItems();
        System.out.print("Seleziona il numero del bene da eliminare (0 per annullare): ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            if (index <= 0 || index > items.size()) return;

            ItemBean selected = items.get(index - 1);
            controller.removeItem(selected);
            System.out.println("Bene '" + selected.getItemName() + "' rimosso correttamente.");
            System.out.println("(Le prenotazioni associate sono state cancellate)");
        } catch (Exception e) {
            // Gestisce ItemInUseException (Step 9c)
            System.err.println("Errore: " + e.getMessage());
        }
    }
}