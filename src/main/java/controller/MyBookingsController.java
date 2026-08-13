package controller;

import exceptions.UnauthorizedOperationException;
import model.bean.BookingBean;
import model.bean.BookingStateBean;
import model.dao.BookingDAO;
import model.dao.GroupDAO;
import model.entity.Booking;
import model.entity.Group;
import model.entity.Item;
import model.entity.Role;
import model.entity.User;
import model.session.SessionContext;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Controller applicativo del caso d'uso "Le mie prenotazioni". */
public class MyBookingsController {
    private final GroupDAO groupDAO;
    private final BookingDAO bookingDAO;
    private final Clock clock;
    private final SessionContext session;

    public MyBookingsController(GroupDAO groupDAO, BookingDAO bookingDAO,
                                SessionContext session) {
        this(groupDAO, bookingDAO, session, Clock.systemDefaultZone());
    }

    MyBookingsController(GroupDAO groupDAO, BookingDAO bookingDAO,
                         SessionContext session, Clock clock) {
        this.groupDAO = Objects.requireNonNull(groupDAO);
        this.bookingDAO = Objects.requireNonNull(bookingDAO);
        this.session = Objects.requireNonNull(session);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<BookingBean> getMyBookings() {
        User operator = requireOperator();
        LocalDateTime now = LocalDateTime.now(clock);
        List<Booking> bookings = bookingDAO.findByOperator(operator.getUsername());
        if (bookings.isEmpty()) {
            return List.of();
        }
        Map<Integer, Group> groupsById = groupDAO.findAll().stream()
                .collect(Collectors.toMap(Group::getGroupID, Function.identity()));

        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getDate)
                        .thenComparing(Booking::getStartTime))
                .map(booking -> toBean(booking, groupsById.get(booking.getGroupId()), now))
                .toList();
    }

    public void deleteBooking(String bookingId)
            throws UnauthorizedOperationException {
        User operator = requireOperator();
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Seleziona una prenotazione.");
        }

        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("La prenotazione non esiste più.");
        }
        if (!booking.getOperatorUsername().equals(operator.getUsername())) {
            throw new UnauthorizedOperationException(
                    "Non puoi eliminare la prenotazione di un altro operatore.");
        }
        if (!booking.canBeDeletedAt(LocalDateTime.now(clock))) {
            throw new IllegalStateException(
                    "Una prenotazione già iniziata non può essere eliminata.");
        }

        bookingDAO.deleteByIds(List.of(bookingId));
    }

    private User requireOperator() {
        User user = session.requireCurrentUser();
        if (user.getRole() != Role.OPERATOR) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return user;
    }

    private BookingBean toBean(Booking booking, Group group, LocalDateTime now) {
        String groupName = group == null ? "Gruppo " + booking.getGroupId() : group.getName();
        Item item = group == null ? null : group.getSingleItemById(booking.getItemId());
        String itemName = item == null ? "Item " + booking.getItemId() : item.getName();
        return new BookingBean(booking.getBookingId(), groupName, itemName,
                booking.getDate(), booking.getStartTime(), booking.getEndTime(),
                new BookingStateBean(booking.canBeDeletedAt(now),
                        booking.canBeReturnedAt(now), booking.getReturnCondition()));
    }
}
