package controller;

import exceptions.UnauthorizedOperationException;
import model.bean.BookingBean;
import model.bean.UserBean;
import model.dao.BookingDAO;
import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.Booking;
import model.entity.Group;
import model.entity.Item;
import model.entity.Operator;
import model.entity.User;

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
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final BookingDAO bookingDAO;
    private final Clock clock;

    public MyBookingsController(UserDAO userDAO, GroupDAO groupDAO, BookingDAO bookingDAO) {
        this(userDAO, groupDAO, bookingDAO, Clock.systemDefaultZone());
    }

    MyBookingsController(UserDAO userDAO, GroupDAO groupDAO,
                         BookingDAO bookingDAO, Clock clock) {
        this.userDAO = Objects.requireNonNull(userDAO);
        this.groupDAO = Objects.requireNonNull(groupDAO);
        this.bookingDAO = Objects.requireNonNull(bookingDAO);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<BookingBean> getMyBookings(UserBean operatorBean) {
        Operator operator = requireOperator(operatorBean);
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

    public void deleteBooking(String bookingId, UserBean operatorBean)
            throws UnauthorizedOperationException {
        Operator operator = requireOperator(operatorBean);
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

    private Operator requireOperator(UserBean operatorBean) {
        User user = operatorBean == null || operatorBean.getUsername() == null
                ? null : userDAO.findByUsername(operatorBean.getUsername());
        if (!(user instanceof Operator operator)) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return operator;
    }

    private BookingBean toBean(Booking booking, Group group, LocalDateTime now) {
        String groupName = group == null ? "Gruppo " + booking.getGroupId() : group.getName();
        Item item = group == null ? null : group.getSingleItemById(booking.getItemId());
        String itemName = item == null ? "Item " + booking.getItemId() : item.getName();
        return new BookingBean(booking.getBookingId(), groupName, itemName,
                booking.getDate(), booking.getStartTime(), booking.getEndTime(),
                booking.canBeDeletedAt(now));
    }
}
