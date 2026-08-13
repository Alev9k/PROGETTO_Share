package boundary.cli;

import controller.ManageItemsController;
import model.bean.CreateItemBean;
import model.bean.ItemBean;

import java.util.List;
import java.util.Scanner;

/** Boundary CLI per visualizzazione e creazione degli item. */
public class ManageItemsBoundaryCLI {
    private final ManageItemsController controller;
    private final CliInput input;

    public ManageItemsBoundaryCLI(ManageItemsController controller, Scanner scanner) {
        this.controller = controller;
        this.input = new CliInput(scanner);
    }

    public void start() {
        while (true) {
            System.out.println("\n--- GESTIONE ITEM ---");
            System.out.println("1. Visualizza item");
            System.out.println("2. Crea item");
            System.out.println("0. Torna al gruppo");
            switch (input.readChoice("Scelta: ", 0, 2)) {
                case 1 -> showItems();
                case 2 -> createItem();
                case 0 -> {
                    return;
                }
                default -> throw new IllegalStateException("Scelta item non prevista.");
            }
        }
    }

    private void showItems() {
        try {
            List<ItemBean> items = controller.getItemList();
            if (items.isEmpty()) {
                System.out.println("Non ci sono item in questo gruppo.");
                return;
            }
            System.out.println("\n--- ITEM DEL GRUPPO ---");
            for (int i = 0; i < items.size(); i++) {
                ItemBean item = items.get(i);
                System.out.println((i + 1) + ". " + item.getItemName()
                        + " | priorità " + item.getPriority()
                        + " | massimo " + item.getMaxUsageTime() + " minuti"
                        + " | " + item.getStatusLabel());
            }
        } catch (Exception e) {
            System.err.println("Impossibile caricare gli item: " + e.getMessage());
        }
    }

    private void createItem() {
        String name = input.readRequired("Nome dell'item: ");
        int priority = input.readInt("Priorità (1-5): ");
        int maxUsageTime = input.readInt(
                "Durata massima in minuti, multipla di 30: ");
        try {
            ItemBean created = controller.createItem(
                    new CreateItemBean(name, priority, maxUsageTime));
            System.out.println("Item '" + created.getItemName() + "' creato correttamente.");
        } catch (Exception e) {
            System.err.println("Impossibile creare l'item: " + e.getMessage());
        }
    }
}
