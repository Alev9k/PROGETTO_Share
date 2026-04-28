package model.entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class Booking {
    private LocalDate date;      // Nel VOPC indicato come int, meglio LocalDate per Java
    private LocalTime startTime; // Nel VOPC indicato come int, meglio LocalTime
    private String itemName;
    private String operatorName;
    private int groupID;

    public Booking(LocalDate date, LocalTime startTime, String itemName, String operatorName, int groupID) {
        this.date = date;
        this.startTime = startTime;
        this.itemName = itemName;
        this.operatorName = operatorName;
        this.groupID = groupID;
    }

    // --- Metodi richiesti dal VOPC ---

    public String getOperatorName() {
        return operatorName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public String getItemName() {
        return itemName;
    }

    public int getGroupID() {
        return groupID;
    }

    /**
     * Verifica la validità temporale (Requisito Funzionale n. 5)
     * "Limite alla giornata corrente e quella successiva"
     */
    public boolean isWithinAllowedTimeframe() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        return date.equals(today) || date.equals(tomorrow);
    }
}