package controller;

import model.bean.BookingBean;
import model.bean.BookingAvailabilityBean;
import model.bean.BookingRequestBean;
import model.bean.ItemBean;
import model.bean.OperatorGroupBean;
import model.dao.BookingDAO;
import model.dao.GroupDAO;
import model.entity.Booking;
import model.entity.BookingSchedule;
import model.entity.BookingSlot;
import model.entity.Group;
import model.entity.Item;
import model.entity.ItemStatus;
import model.entity.Role;
import model.entity.User;
import model.session.SessionContext;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Controller applicativo del caso d'uso "Prenota item". */
public class BookItemController {
    private final GroupDAO groupDAO;
    private final BookingDAO bookingDAO;
    private final Clock clock;
    private final SessionContext session;

    public BookItemController(GroupDAO groupDAO, BookingDAO bookingDAO,
                              SessionContext session) {
        this(groupDAO, bookingDAO, session, Clock.systemDefaultZone());
    }

    BookItemController(GroupDAO groupDAO, BookingDAO bookingDAO,
                       SessionContext session, Clock clock) {
        this.groupDAO = Objects.requireNonNull(groupDAO);
        this.bookingDAO = Objects.requireNonNull(bookingDAO);
        this.session = Objects.requireNonNull(session);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<OperatorGroupBean> getMyGroups() {
        User operator = requireOperator();
        return groupDAO.findGroupsByMemberUsername(operator.getUsername()).stream()
                .map(group -> toOperatorGroupBean(group, operator.getUsername()))
                .toList();
    }

    public List<LocalDate> getBookableDates() {
        LocalDate today = LocalDate.now(clock);
        return List.of(today, today.plusDays(1));
    }

    public List<ItemBean> getBookableItems(int groupId) {
        User operator = requireOperator();
        Group group = requireActiveMembership(groupId, operator.getUsername());
        return group.getItems().stream()
                .filter(item -> item.getStatus() != ItemStatus.BROKEN)
                .sorted(Comparator.comparingInt(Item::getPriority).reversed()
                        .thenComparing(Item::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toItemBean)
                .toList();
    }

    public BookingAvailabilityBean getAvailability(int groupId, int itemId,
                                                   LocalDate date) {
        User operator = requireOperator();
        BookingSchedule schedule = buildSchedule(groupId, itemId, date,
                operator.getUsername());
        return new BookingAvailabilityBean(groupId, itemId, date,
                schedule.getAvailableDurationsByStart(operator.getUsername()));
    }

    public BookingBean createBooking(BookingRequestBean request) {
        if (request == null) {
            throw new IllegalArgumentException("I dati della prenotazione sono obbligatori.");
        }
        User operator = requireOperator();
        validateDate(request.getDate());
        Group group = requireActiveMembership(request.getGroupId(), operator.getUsername());
        Item item = requireItem(group, request.getItemId());
        Booking booking = new Booking(UUID.randomUUID().toString(), group.getGroupID(),
                item.getItemID(), operator.getUsername(), new BookingSlot(request.getDate(),
                request.getStartTime(), request.getDurationMinutes()));

        BookingSchedule schedule = new BookingSchedule(group, item, request.getDate(),
                List.of(), clock);
        schedule.validate(booking);
        bookingDAO.save(booking);
        return new BookingBean(booking.getBookingId(), group.getName(), item.getName(),
                booking.getDate(), booking.getStartTime(), booking.getEndTime());
    }

    private BookingSchedule buildSchedule(int groupId, int itemId, LocalDate date,
                                          String operatorUsername) {
        validateDate(date);
        Group group = requireActiveMembership(groupId, operatorUsername);
        Item item = requireItem(group, itemId);
        return new BookingSchedule(group, item, date, bookingDAO.findByDate(date), clock);
    }

    private void validateDate(LocalDate date) {
        LocalDate today = LocalDate.now(clock);
        if (date == null || (!date.equals(today) && !date.equals(today.plusDays(1)))) {
            throw new IllegalArgumentException("Puoi prenotare solamente per oggi o domani.");
        }
    }

    private User requireOperator() {
        User user = session.requireCurrentUser();
        if (user.getRole() != Role.OPERATOR) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return user;
    }

    private Group requireActiveMembership(int groupId, String operatorUsername) {
        Group group = groupDAO.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Gruppo non trovato.");
        }
        if (!group.isActiveMember(operatorUsername)) {
            throw new IllegalStateException("Non sei un membro attivo di questo gruppo.");
        }
        return group;
    }

    private Item requireItem(Group group, int itemId) {
        Item item = group.getSingleItemById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item non trovato nel gruppo selezionato.");
        }
        if (item.getStatus() == ItemStatus.BROKEN) {
            throw new IllegalStateException("L'item selezionato è guasto.");
        }
        return item;
    }

    private OperatorGroupBean toOperatorGroupBean(Group group, String operatorUsername) {
        return new OperatorGroupBean(group.getGroupID(), group.getName(), group.getOpenTime(),
                group.getCloseTime(), group.isActiveMember(operatorUsername));
    }

    private ItemBean toItemBean(Item item) {
        return new ItemBean(item.getItemID(), item.getName(), item.getPriority(),
                item.getMaxUsageTime(), item.getStatus());
    }
}
