package model.entity;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Agenda giornaliera che applica le regole di disponibilità di un item. */
public class BookingSchedule {
    public static final int SLOT_MINUTES = 30;

    private final Group group;
    private final Item item;
    private final LocalDate date;
    private final List<Booking> bookings;
    private final Clock clock;

    public BookingSchedule(Group group, Item item, LocalDate date,
                           List<Booking> bookings, Clock clock) {
        this.group = Objects.requireNonNull(group);
        this.item = Objects.requireNonNull(item);
        this.date = Objects.requireNonNull(date);
        this.bookings = List.copyOf(Objects.requireNonNull(bookings));
        this.clock = Objects.requireNonNull(clock);
        if (item.getGroupID() != group.getGroupID()) {
            throw new IllegalArgumentException("L'item non appartiene al gruppo selezionato.");
        }
    }

    public List<LocalTime> getAvailableStartSlots(String operatorUsername) {
        return List.copyOf(getAvailableDurationsByStart(operatorUsername).keySet());
    }

    public Map<LocalTime, List<Integer>> getAvailableDurationsByStart(
            String operatorUsername) {
        if (!canEvaluate(operatorUsername)) {
            return Map.of();
        }

        Map<LocalTime, List<Integer>> availability = new LinkedHashMap<>();
        LocalTime candidate = group.getOpenTime();
        LocalTime minimumStart = minimumAllowedStart();
        while (candidate.plusMinutes(SLOT_MINUTES).compareTo(group.getCloseTime()) <= 0) {
            if (!candidate.isBefore(minimumStart)) {
                List<Integer> durations = getAvailableDurations(operatorUsername, candidate);
                if (!durations.isEmpty()) {
                    availability.put(candidate, durations);
                }
            }
            candidate = candidate.plusMinutes(SLOT_MINUTES);
        }
        return Collections.unmodifiableMap(availability);
    }

    public List<Integer> getAvailableDurations(String operatorUsername, LocalTime startTime) {
        if (!canEvaluate(operatorUsername) || !isValidStart(startTime)) {
            return List.of();
        }

        List<Integer> durations = new ArrayList<>();
        for (int duration = SLOT_MINUTES;
             duration <= item.getMaxUsageTime(); duration += SLOT_MINUTES) {
            LocalTime endTime = startTime.plusMinutes(duration);
            if (!endTime.isAfter(startTime) || endTime.isAfter(group.getCloseTime())) {
                break;
            }
            if (hasConflict(operatorUsername, startTime, endTime)) {
                break;
            }
            durations.add(duration);
        }
        return durations;
    }

    public void validate(Booking booking) {
        Objects.requireNonNull(booking, "La prenotazione è obbligatoria.");
        if (booking.getGroupId() != group.getGroupID()
                || booking.getItemId() != item.getItemID()
                || !booking.getDate().equals(date)) {
            throw new IllegalArgumentException("La prenotazione non appartiene all'agenda selezionata.");
        }
        if (!getAvailableDurations(booking.getOperatorUsername(), booking.getStartTime())
                .contains(booking.getDurationMinutes())) {
            throw new IllegalStateException("Lo slot selezionato non è più disponibile.");
        }
    }

    private boolean canEvaluate(String operatorUsername) {
        LocalDate today = LocalDate.now(clock);
        return operatorUsername != null
                && group.isActiveMember(operatorUsername)
                && item.getStatus() != ItemStatus.BROKEN
                && isAllowedDate()
                && (!date.equals(today) || LocalTime.now(clock).isBefore(group.getCloseTime()));
    }

    private boolean isAllowedDate() {
        LocalDate today = LocalDate.now(clock);
        return date.equals(today) || date.equals(today.plusDays(1));
    }

    private boolean isValidStart(LocalTime startTime) {
        if (startTime == null || startTime.getSecond() != 0 || startTime.getNano() != 0
                || startTime.getMinute() % SLOT_MINUTES != 0) {
            return false;
        }
        return !startTime.isBefore(group.getOpenTime())
                && !startTime.isBefore(minimumAllowedStart())
                && startTime.plusMinutes(SLOT_MINUTES).compareTo(group.getCloseTime()) <= 0;
    }

    private LocalTime minimumAllowedStart() {
        LocalDate today = LocalDate.now(clock);
        if (!date.equals(today)) {
            return group.getOpenTime();
        }
        LocalTime roundedNow = roundUpToNextSlot(LocalTime.now(clock));
        return roundedNow.isAfter(group.getOpenTime()) ? roundedNow : group.getOpenTime();
    }

    private LocalTime roundUpToNextSlot(LocalTime time) {
        boolean hasPartialMinute = time.getSecond() != 0 || time.getNano() != 0;
        LocalTime rounded = time.withSecond(0).withNano(0);
        int remainder = rounded.getMinute() % SLOT_MINUTES;
        if (remainder != 0) {
            rounded = rounded.plusMinutes(SLOT_MINUTES - remainder);
        } else if (hasPartialMinute) {
            rounded = rounded.plusMinutes(SLOT_MINUTES);
        }
        return rounded;
    }

    private boolean hasConflict(String operatorUsername, LocalTime startTime,
                                LocalTime endTime) {
        return bookings.stream()
                .anyMatch(booking -> booking.conflictsWith(operatorUsername,
                        group.getGroupID(), item.getItemID(), date, startTime, endTime));
    }
}
