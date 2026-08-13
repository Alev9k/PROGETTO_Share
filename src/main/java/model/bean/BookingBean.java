package model.bean;

import model.entity.ReturnCondition;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class BookingBean {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String bookingId;
    private final String groupName;
    private final String itemName;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final BookingStateBean state;

    public BookingBean(String bookingId, String groupName, String itemName,
                       LocalDate date, LocalTime startTime, LocalTime endTime) {
        this(bookingId, groupName, itemName, date, startTime, endTime, false);
    }

    public BookingBean(String bookingId, String groupName, String itemName,
                       LocalDate date, LocalTime startTime, LocalTime endTime,
                       boolean deletable) {
        this(bookingId, groupName, itemName, date, startTime, endTime,
                new BookingStateBean(deletable, false, ReturnCondition.NOT_REPORTED));
    }

    public BookingBean(String bookingId, String groupName, String itemName,
                       LocalDate date, LocalTime startTime, LocalTime endTime,
                       BookingStateBean state) {
        this.bookingId = bookingId;
        this.groupName = groupName;
        this.itemName = itemName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.state = Objects.requireNonNull(state);
    }

    public String getBookingId() { return bookingId; }
    public String getGroupName() { return groupName; }
    public String getItemName() { return itemName; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isDeletable() { return state.isDeletable(); }
    public boolean isReturnable() { return state.isReturnable(); }
    public ReturnCondition getReturnCondition() { return state.getReturnCondition(); }
    public String getDateLabel() { return DATE_FORMAT.format(date); }
    public String getTimeRangeLabel() {
        return TIME_FORMAT.format(startTime) + " - " + TIME_FORMAT.format(endTime);
    }
    public String getDurationLabel() {
        return ChronoUnit.MINUTES.between(startTime, endTime) + " minuti";
    }
    public String getDeletionLabel() {
        return isDeletable() ? "Sì" : "No";
    }
    public String getReturnLabel() {
        return switch (getReturnCondition()) {
            case NOT_REPORTED -> getNotReportedLabel();
            case INTACT -> "Intatto";
            case BROKEN -> "Guasto";
        };
    }

    private String getNotReportedLabel() {
        if (isReturnable()) {
            return "Da riconsegnare";
        }
        if (isDeletable()) {
            return "Non iniziata";
        }
        return "Non compilato";
    }
}
