package controller;

import exceptions.UnauthorizedOperationException;
import model.dao.BookingDAO;
import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.Booking;
import model.entity.Group;
import model.entity.Item;
import model.entity.ItemStatus;
import model.entity.Operator;
import model.entity.ReturnCondition;
import model.entity.User;
import model.observer.ItemObserver;
import model.session.SessionContext;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Controller applicativo del caso d'uso "Riconsegna item". */
public class ReturnItemController {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final BookingDAO bookingDAO;
    private final List<ItemObserver> itemObservers;
    private final Clock clock;
    private final SessionContext session;

    public ReturnItemController(UserDAO userDAO, GroupDAO groupDAO,
                                BookingDAO bookingDAO,
                                List<ItemObserver> itemObservers,
                                SessionContext session) {
        this(userDAO, groupDAO, bookingDAO, itemObservers, session,
                Clock.systemDefaultZone());
    }

    ReturnItemController(UserDAO userDAO, GroupDAO groupDAO,
                         BookingDAO bookingDAO, List<ItemObserver> itemObservers,
                         SessionContext session, Clock clock) {
        this.userDAO = Objects.requireNonNull(userDAO);
        this.groupDAO = Objects.requireNonNull(groupDAO);
        this.bookingDAO = Objects.requireNonNull(bookingDAO);
        this.itemObservers = List.copyOf(Objects.requireNonNull(itemObservers));
        this.session = Objects.requireNonNull(session);
        this.clock = Objects.requireNonNull(clock);
    }

    public void returnItem(String bookingId, boolean broken)
            throws UnauthorizedOperationException {
        Operator operator = requireOperator();
        Booking booking = requireOwnedBooking(bookingId, operator.getUsername());
        Group group = groupDAO.findGroupById(booking.getGroupId());
        if (group == null) {
            throw new IllegalStateException("Il gruppo della prenotazione non esiste più.");
        }
        Item item = group.getSingleItemById(booking.getItemId());
        if (item == null) {
            throw new IllegalStateException("L'item della prenotazione non esiste più.");
        }
        if (broken && item.getStatus() == ItemStatus.BROKEN) {
            throw new IllegalStateException("L'item è già stato segnalato come guasto.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        ReturnCondition condition = broken ? ReturnCondition.BROKEN : ReturnCondition.INTACT;
        booking.registerReturn(condition, now);

        if (broken) {
            notifyBrokenItem(item, operator.getUsername(), now);
            groupDAO.update(group);
        }
        bookingDAO.update(booking);
    }

    private void notifyBrokenItem(Item item, String operatorUsername, LocalDateTime now) {
        itemObservers.forEach(item::attach);
        try {
            item.markAsBroken(operatorUsername, now);
        } finally {
            itemObservers.forEach(item::detach);
        }
    }

    private Booking requireOwnedBooking(String bookingId, String operatorUsername)
            throws UnauthorizedOperationException {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Seleziona una prenotazione.");
        }
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("La prenotazione non esiste più.");
        }
        if (!booking.getOperatorUsername().equals(operatorUsername)) {
            throw new UnauthorizedOperationException(
                    "Non puoi riconsegnare la prenotazione di un altro operatore.");
        }
        return booking;
    }

    private Operator requireOperator() {
        User user = userDAO.findByUsername(session.requireCurrentUser().getUsername());
        if (!(user instanceof Operator operator)) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return operator;
    }
}
