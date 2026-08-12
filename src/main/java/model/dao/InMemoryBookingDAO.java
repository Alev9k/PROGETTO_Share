package model.dao;

import exceptions.DAOException;
import model.entity.Booking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class InMemoryBookingDAO implements BookingDAO {
    private final List<Booking> bookings = new ArrayList<>();

    @Override
    public synchronized void save(Booking booking) {
        Objects.requireNonNull(booking);
        if (findById(booking.getBookingId()) != null) {
            throw new DAOException("Esiste già una prenotazione con questo identificativo.");
        }
        BookingDAO.requireNoConflicts(bookings, booking);
        bookings.add(booking);
    }

    @Override
    public synchronized void deleteByIds(Collection<String> bookingIds) {
        Set<String> ids = new HashSet<>(Objects.requireNonNull(bookingIds));
        if (ids.isEmpty()) {
            return;
        }
        long found = bookings.stream()
                .filter(booking -> ids.contains(booking.getBookingId()))
                .count();
        if (found != ids.size()) {
            throw new DAOException("Una o più prenotazioni non sono state trovate.");
        }
        bookings.removeIf(booking -> ids.contains(booking.getBookingId()));
    }

    @Override
    public synchronized List<Booking> findAll() {
        return new ArrayList<>(bookings);
    }
}
