package model.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/** Value object che rappresenta lo slot temporale occupato da una prenotazione. */
public class BookingSlot {
    private final LocalDate date;
    private final LocalTime startTime;
    private final int durationMinutes;

    public BookingSlot(LocalDate date, LocalTime startTime, int durationMinutes) {
        if (durationMinutes <= 0
                || durationMinutes % BookingSchedule.SLOT_MINUTES != 0) {
            throw new IllegalArgumentException("La durata deve essere un multiplo di 30 minuti.");
        }
        LocalTime validStartTime = Objects.requireNonNull(
                startTime, "L'orario iniziale è obbligatorio.");
        if (validStartTime.getMinute() % BookingSchedule.SLOT_MINUTES != 0
                || validStartTime.getSecond() != 0 || validStartTime.getNano() != 0) {
            throw new IllegalArgumentException(
                    "L'orario iniziale deve coincidere con uno slot di 30 minuti.");
        }
        this.date = Objects.requireNonNull(date, "La data è obbligatoria.");
        this.startTime = validStartTime;
        this.durationMinutes = durationMinutes;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public LocalTime getEndTime() {
        return startTime.plusMinutes(durationMinutes);
    }
}
