package model.bean;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingRequestBean {
    private final int groupId;
    private final int itemId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final int durationMinutes;

    public BookingRequestBean(int groupId, int itemId, LocalDate date,
                              LocalTime startTime, int durationMinutes) {
        this.groupId = groupId;
        this.itemId = itemId;
        this.date = date;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
    }

    public int getGroupId() { return groupId; }
    public int getItemId() { return itemId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
}
