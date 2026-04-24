package share.model.entity;

import java.util.ArrayList;
import java.util.List;

public class Operator {
    private String username;
    private List<Booking> bookingsList;
    private List<State> stateList;
    public Operator(String username) {
        this.username = username;
        this.bookingsList = new ArrayList<>();
        this.stateList = new ArrayList<>();
    }

    // Verifica se l'operatore è attivo in un determinato gruppo
    public boolean checkActiveness(int groupID) {
        for (State s : stateList) {
            if (s.getGroupID() == groupID) {
                return s.getStatus() == 0; // Ritorna true se lo stato è 0 (ATTIVO)
            }
        }
        return false;
    }

    /**
     * Metodo richiesto dal VOPC per cambiare lo stato dell'operatore.
     * Se è ATTIVO (0) diventa BLOCCATO (1), e viceversa.
     */
    public void toggleState(int groupID) {
        for (State s : stateList) {
            if (s.getGroupID() == groupID) {
                if (s.getStatus() == 0) {
                    s.setStatus(1); // Diventa BLOCCATO
                } else {
                    s.setStatus(0); // Ritorna ATTIVO
                }
                return;
            }
        }
    }

    // Cancella tutte le prenotazioni di un operatore in un gruppo specifico
    public List<Booking> cancelGroupBookings(int groupID) {
        List<Booking> removedBookings = new ArrayList<>();
        // Rimuoviamo i booking che corrispondono al groupID
        this.bookingsList.removeIf(b -> {
            if (b.getGroupID() == groupID) {
                removedBookings.add(b);
                return true;
            }
            return false;
        });
        return removedBookings;
    }

    public String getUsername() { return username; }
}