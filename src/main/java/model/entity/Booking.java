package model.entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class Booking {
    private final LocalDate date;      // Nel VOPC indicato come int, meglio LocalDate per Java
    private final LocalTime startTime; // Nel VOPC indicato come int, meglio LocalTime
    private final String itemName;
    private final String operatorName;
    private final int groupID;

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