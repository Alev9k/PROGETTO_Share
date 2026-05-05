package model.entity;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private final String name;           // Nome univoco nel gruppo
    private final String assetName;      // Nome dell'asset da cui eredita
    private int status;            // 0: Disponibile, 1: In Uso, 2: Guasto
    private List<Booking> bookingsList; // Elenco prenotazioni

    public Item(String name, String assetName) {
        this.name = name;
        this.assetName = assetName;
        this.status = 1; // Inizialmente disponibile
        this.bookingsList = new ArrayList<>();
    }

    // Verificare se l'item è in uso
    public boolean checkActiveness() {
        return this.status == 1; // Ritorna true se lo stato è "In Uso"
    }

    // Aggiungi questo metodo in model.entity.Item
    public void removeBooking(Booking booking) {
        // Rimuove l'oggetto booking specifico dalla lista delle prenotazioni del bene
        this.bookingsList.remove(booking);
    }

    public List<Booking> getBookings() {
        return this.bookingsList;
    }

    // Getter per il nome (necessario per il controllo unicità)
    public String getName() {
        return name;
    }
    public String getAssetName() {
        return assetName;
    }
}
