package model.dao;

import exceptions.DAOException;
import model.entity.Booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public synchronized void update(Booking updatedBooking) {
        Objects.requireNonNull(updatedBooking);
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getBookingId().equals(updatedBooking.getBookingId())) {
                bookings.set(i, updatedBooking);
                return;
            }
        }
        throw new DAOException("Prenotazione non trovata.");
    }

    @Override
    public synchronized List<Booking> findAll() {
        return new ArrayList<>(bookings);
    }
}
