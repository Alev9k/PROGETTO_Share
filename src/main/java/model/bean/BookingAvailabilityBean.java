package model.bean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fotografia immutabile della disponibilità giornaliera di un item. */
public class BookingAvailabilityBean {
    private final int groupId;
    private final int itemId;
    private final LocalDate date;
    private final Map<LocalTime, List<Integer>> durationsByStart;

    public BookingAvailabilityBean(int groupId, int itemId, LocalDate date,
                                   Map<LocalTime, List<Integer>> durationsByStart) {
        this.groupId = groupId;
        this.itemId = itemId;
        this.date = date;

        Map<LocalTime, List<Integer>> copiedAvailability = new LinkedHashMap<>();
        durationsByStart.forEach((start, durations) ->
                copiedAvailability.put(start, List.copyOf(durations)));
        this.durationsByStart = Collections.unmodifiableMap(copiedAvailability);
    }

    public int getGroupId() {
        return groupId;
    }

    public int getItemId() {
        return itemId;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<LocalTime> getStartSlots() {
        return List.copyOf(durationsByStart.keySet());
    }

    public List<Integer> getDurationsFor(LocalTime startTime) {
        return durationsByStart.getOrDefault(startTime, List.of());
    }
}
