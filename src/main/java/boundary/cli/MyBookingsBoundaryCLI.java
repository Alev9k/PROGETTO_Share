package boundary.cli;

import controller.MyBookingsController;
import controller.ReturnItemController;
import model.bean.BookingBean;
import model.entity.ReturnCondition;

import java.util.List;
import java.util.Scanner;

/** Boundary CLI dei casi d'uso sulle prenotazioni dell'operatore. */
public class MyBookingsBoundaryCLI {
    private final MyBookingsController bookingsController;
    private final ReturnItemController returnController;
    private final CliInput input;

    public MyBookingsBoundaryCLI(MyBookingsController bookingsController,
                                 ReturnItemController returnController,
                                 Scanner scanner) {
        this.bookingsController = bookingsController;
        this.returnController = returnController;
        this.input = new CliInput(scanner);
    }

    public void start() {
        while (true) {
            List<BookingBean> bookings;
            try {
                bookings = bookingsController.getMyBookings();
            } catch (RuntimeException e) {
                System.err.println("Impossibile caricare le prenotazioni: " + e.getMessage());
                return;
            }

            System.out.println("\n--- LE MIE PRENOTAZIONI ---");
            if (bookings.isEmpty()) {
                System.out.println("Non hai prenotazioni registrate.");
                return;
            }
            for (int i = 0; i < bookings.size(); i++) {
                BookingBean booking = bookings.get(i);
                System.out.println((i + 1) + ". " + booking.getGroupName() + " | "
                        + booking.getItemName() + " | " + booking.getDateLabel() + " | "
                        + booking.getTimeRangeLabel() + " | " + booking.getReturnLabel());
            }
            System.out.println("0. Torna alla dashboard");
            int choice = input.readChoice("Seleziona una prenotazione: ", 0, bookings.size());
            if (choice == 0) {
                return;
            }
            manage(bookings.get(choice - 1));
        }
    }

    private void manage(BookingBean booking) {
        System.out.println("\nPrenotazione selezionata: " + booking.getItemName()
                + " del " + booking.getDateLabel() + " " + booking.getTimeRangeLabel());
        if (!booking.isDeletable() && !booking.isReturnable()) {
            System.out.println(booking.getReturnCondition() == ReturnCondition.NOT_REPORTED
                    ? "Non sono disponibili operazioni per questa prenotazione."
                    : "La riconsegna è già stata registrata: " + booking.getReturnLabel() + ".");
            return;
        }

        System.out.println("1. Elimina prenotazione"
                + (booking.isDeletable() ? "" : " [non disponibile]"));
        System.out.println("2. Riconsegna item"
                + (booking.isReturnable() ? "" : " [non disponibile]"));
        System.out.println("0. Indietro");
        int action = input.readChoice("Scelta: ", 0, 2);
        if (action == 1) {
            deleteBooking(booking);
        } else if (action == 2) {
            returnItem(booking);
        }
    }

    private void deleteBooking(BookingBean booking) {
        if (!booking.isDeletable()) {
            System.out.println("La prenotazione è già iniziata e non può essere eliminata.");
            return;
        }
        if (!input.confirm("Confermi l'eliminazione della prenotazione?")) {
            return;
        }
        try {
            bookingsController.deleteBooking(booking.getBookingId());
            System.out.println("Prenotazione eliminata; lo slot è nuovamente disponibile.");
        } catch (Exception e) {
            System.err.println("Impossibile eliminare la prenotazione: " + e.getMessage());
        }
    }

    private void returnItem(BookingBean booking) {
        if (!booking.isReturnable()) {
            System.out.println("L'item non può essere riconsegnato in questo momento.");
            return;
        }
        System.out.println("1. Intatto");
        System.out.println("2. Guasto");
        System.out.println("0. Annulla");
        int condition = input.readChoice("Condizione dell'item: ", 0, 2);
        if (condition == 0) {
            return;
        }
        boolean broken = condition == 2;
        if (broken && !input.confirm(
                "La segnalazione eliminerà le prenotazioni future. Confermi?")) {
            return;
        }
        try {
            returnController.returnItem(booking.getBookingId(), broken);
            System.out.println(broken
                    ? "Item segnalato guasto; Admin e operatori interessati saranno notificati."
                    : "Riconsegna dell'item intatto registrata.");
        } catch (Exception e) {
            System.err.println("Impossibile registrare la riconsegna: " + e.getMessage());
        }
    }
}
