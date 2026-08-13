package model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Booking {
    private final String bookingId;
    private final int groupId;
    private final int itemId;
    private final String operatorUsername;
    private final BookingSlot slot;
    private ReturnCondition returnCondition;

    public Booking(String bookingId, int groupId, int itemId, String operatorUsername,
                   BookingSlot slot) {
        this(bookingId, groupId, itemId, operatorUsername, slot,
                ReturnCondition.NOT_REPORTED);
    }

    public Booking(String bookingId, int groupId, int itemId, String operatorUsername,
                   BookingSlot slot, ReturnCondition returnCondition) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("L'identificativo della prenotazione è obbligatorio.");
        }
        if (groupId <= 0 || itemId <= 0) {
            throw new IllegalArgumentException("Gruppo e item della prenotazione non sono validi.");
        }
        if (operatorUsername == null || operatorUsername.isBlank()) {
            throw new IllegalArgumentException("L'operatore della prenotazione è obbligatorio.");
        }
        this.bookingId = bookingId;
        this.groupId = groupId;
        this.itemId = itemId;
        this.operatorUsername = operatorUsername.trim();
        this.slot = Objects.requireNonNull(slot, "Lo slot della prenotazione è obbligatorio.");
        this.returnCondition = Objects.requireNonNull(returnCondition,
                "Lo stato della riconsegna è obbligatorio.");
    }

    public String getBookingId() { return bookingId; }
    public int getGroupId() { return groupId; }
    public int getItemId() { return itemId; }
    public String getOperatorUsername() { return operatorUsername; }
    public LocalDate getDate() { return slot.getDate(); }
    public LocalTime getStartTime() { return slot.getStartTime(); }
    public int getDurationMinutes() { return slot.getDurationMinutes(); }
    public ReturnCondition getReturnCondition() { return returnCondition; }

    public LocalTime getEndTime() {
        return slot.getEndTime();
    }

    /** Verifica il conflitto tra due prenotazioni persistenti sullo stesso item o operatore. */
    public boolean conflictsWith(Booking other) {
        Objects.requireNonNull(other);
        return conflictsWith(other.operatorUsername, other.groupId,
                other.itemId, other.getDate(), other.getStartTime(), other.getEndTime());
    }

    public boolean overlaps(LocalDate candidateDate, LocalTime candidateStart,
                            LocalTime candidateEnd) {
        return getDate().equals(candidateDate)
                && getStartTime().isBefore(candidateEnd)
                && candidateStart.isBefore(getEndTime());
    }

    boolean conflictsWith(String candidateOperator, int candidateGroupId,
                          int candidateItemId, LocalDate candidateDate,
                          LocalTime candidateStart, LocalTime candidateEnd) {
        boolean sameItem = groupId == candidateGroupId && itemId == candidateItemId;
        boolean sameOperator = operatorUsername.equals(candidateOperator);
        return (sameItem || sameOperator)
                && overlaps(candidateDate, candidateStart, candidateEnd);
    }

    public boolean isInProgress(LocalDateTime moment) {
        Objects.requireNonNull(moment);
        if (!getDate().equals(moment.toLocalDate())) {
            return false;
        }
        LocalTime time = moment.toLocalTime();
        return !time.isBefore(getStartTime()) && time.isBefore(getEndTime());
    }

    public boolean startsAfter(LocalDateTime moment) {
        return LocalDateTime.of(getDate(), getStartTime())
                .isAfter(Objects.requireNonNull(moment));
    }

    public boolean startsAtOrAfter(LocalDateTime moment) {
        return !LocalDateTime.of(getDate(), getStartTime())
                .isBefore(Objects.requireNonNull(moment));
    }

    /** Una prenotazione è eliminabile solo prima che inizi l'utilizzo dell'item. */
    public boolean canBeDeletedAt(LocalDateTime moment) {
        return startsAfter(moment);
    }

    /** La riconsegna è ammessa dall'inizio fino all'istante esatto di scadenza. */
    public boolean canBeReturnedAt(LocalDateTime moment) {
        Objects.requireNonNull(moment);
        LocalDateTime start = LocalDateTime.of(getDate(), getStartTime());
        LocalDateTime end = start.plusMinutes(getDurationMinutes());
        return returnCondition == ReturnCondition.NOT_REPORTED
                && !moment.isBefore(start)
                && !moment.isAfter(end);
    }

    public void registerReturn(ReturnCondition condition, LocalDateTime moment) {
        if (condition == null || condition == ReturnCondition.NOT_REPORTED) {
            throw new IllegalArgumentException("Seleziona se l'item è intatto o guasto.");
        }
        if (returnCondition != ReturnCondition.NOT_REPORTED) {
            throw new IllegalStateException("La riconsegna è già stata registrata.");
        }
        if (!canBeReturnedAt(moment)) {
            throw new IllegalStateException(
                    "La riconsegna è consentita soltanto durante la prenotazione.");
        }
        returnCondition = condition;
    }
}
