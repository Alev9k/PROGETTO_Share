package model.dao;

import exceptions.BookingConflictException;
import model.entity.Booking;
import model.entity.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileBookingDAOTest {
    @Test
    void persistsAndUpdatesBookingStatus(@TempDir Path tempDirectory) {
        Path file = tempDirectory.resolve("bookings.csv");
        FileBookingDAO dao = new FileBookingDAO(file);
        Booking booking = booking("one", 1, 1, "operator", LocalTime.of(10, 0), 60);
        dao.save(booking);

        FileBookingDAO restarted = new FileBookingDAO(file);
        assertEquals(1, restarted.findAll().size());
        assertEquals(BookingStatus.ACTIVE, restarted.findById("one").getStatus());

        booking.cancel();
        restarted.update(booking);
        assertEquals(BookingStatus.CANCELLED,
                new FileBookingDAO(file).findById("one").getStatus());
    }

    @Test
    void rejectsItemAndOperatorConflicts(@TempDir Path tempDirectory) {
        FileBookingDAO dao = new FileBookingDAO(tempDirectory.resolve("bookings.csv"));
        dao.save(booking("first", 1, 1, "operator", LocalTime.of(10, 0), 60));

        assertThrows(BookingConflictException.class, () ->
                dao.save(booking("same-item", 1, 1, "other", LocalTime.of(10, 30), 30)));
        assertThrows(BookingConflictException.class, () ->
                dao.save(booking("same-operator", 1, 2, "operator", LocalTime.of(10, 30), 30)));
        assertEquals(1, dao.findAll().size());
    }

    private Booking booking(String id, int groupId, int itemId, String operator,
                            LocalTime start, int duration) {
        return new Booking(id, groupId, itemId, operator,
                LocalDate.of(2026, 8, 12), start, duration);
    }
}
