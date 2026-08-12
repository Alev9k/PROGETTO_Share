package model.bean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class BookingBean {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String bookingId;
    private final String groupName;
    private final String itemName;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean deletable;

    public BookingBean(String bookingId, String groupName, String itemName,
                       LocalDate date, LocalTime startTime, LocalTime endTime) {
        this(bookingId, groupName, itemName, date, startTime, endTime, false);
    }

    public BookingBean(String bookingId, String groupName, String itemName,
                       LocalDate date, LocalTime startTime, LocalTime endTime,
                       boolean deletable) {
        this.bookingId = bookingId;
        this.groupName = groupName;
        this.itemName = itemName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deletable = deletable;
    }

    public String getBookingId() { return bookingId; }
    public String getGroupName() { return groupName; }
    public String getItemName() { return itemName; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isDeletable() { return deletable; }
    public String getDateLabel() { return DATE_FORMAT.format(date); }
    public String getTimeRangeLabel() {
        return TIME_FORMAT.format(startTime) + " - " + TIME_FORMAT.format(endTime);
    }
    public String getDurationLabel() {
        return ChronoUnit.MINUTES.between(startTime, endTime) + " minuti";
    }
    public String getDeletionLabel() {
        return deletable ? "Sì" : "No";
    }
}
