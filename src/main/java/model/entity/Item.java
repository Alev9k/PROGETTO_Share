package model.entity;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private final int itemID;            // ID univoco dell'oggetto fisico
    private final String name;           // Nome univoco nel gruppo (es. "PC-Lab-01")
    private final int assetID;           // Riferimento al catalogo (Type Object)
    private final int groupID;
    // Nuovi parametri fisici
    private int priority;                // Es. 1 (Bassa) a 5 (Alta)
    private int maxUsageTime;            // Tempo massimo in minuti (o ore)

    private int status;                  // 0: Disponibile, 1: In Uso, 2: Guasto
    private List<Booking> bookingsList;  // Elenco prenotazioni dell'oggetto

    public Item(int itemID, String name, int groupID, int assetID, int priority, int maxUsageTime) {
        this.itemID = itemID;
        this.name = name;
        this.groupID = groupID;
        this.assetID = assetID;
        this.priority = priority;
        this.maxUsageTime = maxUsageTime;
        this.status = 0; // 0 = Disponibile di default
        this.bookingsList = new ArrayList<>();
    }

    public boolean checkActiveness() {
        return this.status == 1; // Ritorna true se lo stato è "In Uso"
    }

    public void removeBooking(Booking booking) {
        this.bookingsList.remove(booking);
    }

    // --- Getters ---
    public int getItemID() { return itemID; }
    public String getName() { return name; }
    public int getGroupID() { return groupID; }
    public int getAssetID() { return assetID; }
    public int getPriority() { return priority; }
    public int getMaxUsageTime() { return maxUsageTime; }
    public int getStatus() { return status; }
    public List<Booking> getBookings() { return bookingsList; }

    // --- Setters (per permettere aggiornamenti fisici e riparazioni) ---
    public void setStatus(int status) { this.status = status; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setMaxUsageTime(int maxUsageTime) { this.maxUsageTime = maxUsageTime; }
}
