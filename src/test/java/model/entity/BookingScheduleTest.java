package model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingScheduleTest {
    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 11);

    private Group group;
    private Item item;
    private Clock clock;

    @BeforeEach
    void setUp() throws Exception {
        clock = Clock.fixed(Instant.parse("2026-08-11T07:10:00Z"), ZONE);
        group = new Group(1, "Laboratorio", LocalTime.of(8, 0),
                LocalTime.of(18, 0), "123456", "admin");
        group.addMember("operator");
        group.addMember("other");
        item = new Item(1, "Trapano", 1, 4, 120);
        group.addItem(item);
    }

    @Test
    void availabilityStopsBeforeAnExistingItemBooking() {
        Booking occupied = booking("occupied", 1, 1, "other",
                TODAY.plusDays(1), LocalTime.of(10, 0), 90);
        BookingSchedule schedule = new BookingSchedule(group, item, TODAY.plusDays(1),
                List.of(occupied), clock);

        assertEquals(List.of(30, 60),
                schedule.getAvailableDurations("operator", LocalTime.of(9, 0)));
        assertEquals(List.of(30),
                schedule.getAvailableDurations("operator", LocalTime.of(9, 30)));
        assertFalse(schedule.getAvailableStartSlots("operator").contains(LocalTime.of(10, 0)));
        assertEquals(List.of(30, 60, 90, 120),
                schedule.getAvailableDurations("operator", LocalTime.of(11, 30)));
    }

    @Test
    void operatorCannotOverlapAnotherOwnBookingEvenOnAnotherItem() {
        Booking ownBooking = booking("own", 2, 7, "operator",
                TODAY.plusDays(1), LocalTime.of(14, 0), 60);
        BookingSchedule schedule = new BookingSchedule(group, item, TODAY.plusDays(1),
                List.of(ownBooking), clock);

        assertFalse(schedule.getAvailableStartSlots("operator").contains(LocalTime.of(14, 0)));
        assertTrue(schedule.getAvailableStartSlots("operator").contains(LocalTime.of(15, 0)));
    }

    @Test
    void sameItemIdInAnotherGroupDoesNotOccupyThisItem() {
        Booking otherGroupBooking = booking("other-group", 2, 1, "other",
                TODAY.plusDays(1), LocalTime.of(16, 0), 60);
        BookingSchedule schedule = new BookingSchedule(group, item, TODAY.plusDays(1),
                List.of(otherGroupBooking), clock);

        assertTrue(schedule.getAvailableStartSlots("operator").contains(LocalTime.of(16, 0)));
    }

    @Test
    void todayStartsAtTheNextHalfHour() {
        BookingSchedule schedule = new BookingSchedule(group, item, TODAY, List.of(), clock);

        assertEquals(LocalTime.of(9, 30),
                schedule.getAvailableStartSlots("operator").getFirst());
    }

    @Test
    void blockedMemberOrBrokenItemHasNoAvailability() {
        group.toggleMemberStatus("operator");
        BookingSchedule blockedSchedule = new BookingSchedule(
                group, item, TODAY.plusDays(1), List.of(), clock);
        assertTrue(blockedSchedule.getAvailableStartSlots("operator").isEmpty());

        group.toggleMemberStatus("operator");
        item.setStatus(ItemStatus.BROKEN);
        BookingSchedule brokenSchedule = new BookingSchedule(
                group, item, TODAY.plusDays(1), List.of(), clock);
        assertTrue(brokenSchedule.getAvailableStartSlots("operator").isEmpty());
    }

    private Booking booking(String id, int groupId, int itemId, String username,
                            LocalDate date, LocalTime start, int duration) {
        return new Booking(id, groupId, itemId, username, date, start, duration);
    }
}
