package boundary.cli;

import controller.BookItemController;
import model.bean.BookingAvailabilityBean;
import model.bean.BookingBean;
import model.bean.BookingRequestBean;
import model.bean.ItemBean;
import model.bean.OperatorGroupBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/** Boundary CLI del caso d'uso "Prenota item". */
public class BookItemBoundaryCLI {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final BookItemController controller;
    private final CliInput input;

    public BookItemBoundaryCLI(BookItemController controller, Scanner scanner) {
        this.controller = controller;
        this.input = new CliInput(scanner);
    }

    public void start() {
        while (true) {
            List<OperatorGroupBean> groups;
            try {
                groups = controller.getMyGroups();
            } catch (RuntimeException e) {
                System.err.println("Impossibile caricare i gruppi: " + e.getMessage());
                return;
            }

            System.out.println("\n--- I MIEI GRUPPI ---");
            if (groups.isEmpty()) {
                System.out.println("Non appartieni ancora a un gruppo.");
                return;
            }
            for (int i = 0; i < groups.size(); i++) {
                OperatorGroupBean group = groups.get(i);
                System.out.println((i + 1) + ". " + group.getGroupName() + " | "
                        + TIME_FORMAT.format(group.getOpenTime()) + "-"
                        + TIME_FORMAT.format(group.getCloseTime()) + " | "
                        + group.getStatusLabel());
            }
            System.out.println("0. Torna alla dashboard");
            int groupChoice = input.readChoice("Seleziona un gruppo: ", 0, groups.size());
            if (groupChoice == 0) {
                return;
            }
            OperatorGroupBean selectedGroup = groups.get(groupChoice - 1);
            if (!selectedGroup.isActive()) {
                System.out.println("Il gruppo è visibile, ma la membership è bloccata.");
                continue;
            }
            bookFromGroup(selectedGroup);
        }
    }

    private void bookFromGroup(OperatorGroupBean group) {
        try {
            List<ItemBean> items = controller.getBookableItems(group.getGroupId());
            if (items.isEmpty()) {
                System.out.println("Il gruppo non contiene item prenotabili.");
                return;
            }
            System.out.println("\n--- ITEM PRENOTABILI ---");
            for (int i = 0; i < items.size(); i++) {
                ItemBean item = items.get(i);
                System.out.println((i + 1) + ". " + item.getItemName()
                        + " | priorità " + item.getPriority()
                        + " | massimo " + item.getMaxUsageTime() + " minuti");
            }
            System.out.println("0. Annulla");
            int itemChoice = input.readChoice("Seleziona un item: ", 0, items.size());
            if (itemChoice == 0) {
                return;
            }

            List<LocalDate> dates = controller.getBookableDates();
            System.out.println("1. Oggi - " + DATE_FORMAT.format(dates.get(0)));
            System.out.println("2. Domani - " + DATE_FORMAT.format(dates.get(1)));
            LocalDate date = dates.get(input.readChoice("Seleziona il giorno: ", 1, 2) - 1);
            ItemBean item = items.get(itemChoice - 1);
            BookingAvailabilityBean availability = controller.getAvailability(
                    group.getGroupId(), item.getItemId(), date);
            List<LocalTime> starts = availability.getStartSlots();
            if (starts.isEmpty()) {
                System.out.println("Non ci sono slot liberi per il giorno selezionato.");
                return;
            }

            System.out.println("\nSlot di partenza disponibili:");
            for (int i = 0; i < starts.size(); i++) {
                System.out.println((i + 1) + ". " + TIME_FORMAT.format(starts.get(i)));
            }
            LocalTime start = starts.get(input.readChoice(
                    "Seleziona lo slot: ", 1, starts.size()) - 1);
            List<Integer> durations = availability.getDurationsFor(start);
            System.out.println("Durate disponibili:");
            for (int i = 0; i < durations.size(); i++) {
                System.out.println((i + 1) + ". " + durations.get(i) + " minuti");
            }
            int duration = durations.get(input.readChoice(
                    "Seleziona la durata: ", 1, durations.size()) - 1);

            BookingBean booking = controller.createBooking(new BookingRequestBean(
                    group.getGroupId(), item.getItemId(), date, start, duration));
            System.out.println("Prenotazione confermata: " + booking.getItemName()
                    + " | " + booking.getDateLabel() + " | "
                    + booking.getTimeRangeLabel() + ".");
        } catch (RuntimeException e) {
            System.err.println("Impossibile completare la prenotazione: " + e.getMessage());
        }
    }
}
