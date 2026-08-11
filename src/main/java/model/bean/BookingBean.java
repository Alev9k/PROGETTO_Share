package model.bean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class BookingBean {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String bookingId;
    private final String groupName;
    private final String itemName;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public BookingBean(String bookingId, String groupName, String itemName,
                       LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.bookingId = bookingId;
        this.groupName = groupName;
        this.itemName = itemName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getBookingId() { return bookingId; }
    public String getGroupName() { return groupName; }
    public String getItemName() { return itemName; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getTimeRangeLabel() {
        return TIME_FORMAT.format(startTime) + " - " + TIME_FORMAT.format(endTime);
    }
}
