package model.dao;

import exceptions.BookingConflictException;
import model.entity.Booking;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingDAO {
    void save(Booking booking);
    void update(Booking booking);
    void deleteByIds(Collection<String> bookingIds);
    List<Booking> findAll();

    default Booking findById(String bookingId) {
        return findAll().stream()
                .filter(booking -> booking.getBookingId().equals(bookingId))
                .findFirst()
                .orElse(null);
    }

    default List<Booking> findByDate(LocalDate date) {
        return findAll().stream()
                .filter(booking -> booking.getDate().equals(date))
                .toList();
    }

    default List<Booking> findByOperator(String username) {
        return findAll().stream()
                .filter(booking -> booking.getOperatorUsername().equals(username))
                .toList();
    }

    default List<Booking> findByOperatorAndGroup(String username, int groupId) {
        return findAll().stream()
                .filter(booking -> booking.getGroupId() == groupId)
                .filter(booking -> booking.getOperatorUsername().equals(username))
                .toList();
    }

    static void requireNoConflicts(List<Booking> existingBookings, Booking candidate) {
        boolean conflict = existingBookings.stream()
                .anyMatch(existing -> existing.conflictsWith(candidate));
        if (conflict) {
            throw new BookingConflictException(
                    "Lo slot è stato appena occupato da un'altra prenotazione.");
        }
    }

}
