package model.entity;

import java.util.ArrayList;
import java.util.List;

public class Operator extends User {
    private List<Booking> bookingsList;
    private List<State> stateList;
    public Operator(String username, String password) {
        super(username, password);
        this.bookingsList = new ArrayList<>();
        this.stateList = new ArrayList<>();
    }

    @Override
    public void joinGroup(Group group) {
        if (group.findOperatorByUsername(username) != null) {
            throw new IllegalStateException("L'operatore e gia iscritto al gruppo.");
        }
        group.addOperator(this);
        stateList.add(new State(group.getGroupID()));
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

    public boolean hasItemFromGroup(int groupID) {
        // Controlliamo la lista delle prenotazioni dell'operatore[cite: 1]
        for (Booking booking : bookingsList) {
            // Se troviamo una prenotazione collegata a questo gruppo
            if (booking.getGroupID() == groupID) {
                // In questa logica, la presenza di un booking indica il possesso[cite: 1]
                return true;
            }
        }
        return false; // L'operatore non ha beni di questo gruppo in carico
    }

    public void removeBookingByItem(String itemName, int groupID) {
        // Rimuove dalla lista dell'operatore la prenotazione che corrisponde a quel bene in quel gruppo
        this.bookingsList.removeIf(b ->
                b.getItemName().equals(itemName) && b.getGroupID() == groupID
        );
    }

    public void removeState(int groupID) {
        // Rimuoviamo dalla stateList l'oggetto State che ha il groupID corrispondente
        this.stateList.removeIf(state -> state.getGroupID() == groupID);
    }
}
