package controller;

import model.bean.BookingBean;
import model.bean.BookingAvailabilityBean;
import model.bean.BookingRequestBean;
import model.bean.Role;
import model.bean.UserBean;
import model.dao.InMemoryBookingDAO;
import model.dao.InMemoryGroupDAO;
import model.dao.InMemoryUserDAO;
import model.entity.Booking;
import model.entity.Group;
import model.entity.Item;
import model.entity.Operator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookItemControllerTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 11);

    private InMemoryBookingDAO bookingDAO;
    private BookItemController controller;
    private UserBean operatorBean;
    private Group group;
    private InMemoryUserDAO userDAO;
    private InMemoryGroupDAO groupDAO;
    private Clock clock;

    @BeforeEach
    void setUp() throws Exception {
        String username = "booking-operator-" + UUID.randomUUID();
        userDAO = new InMemoryUserDAO();
        userDAO.save(new Operator(username, "password"));
        operatorBean = new UserBean(username, Role.OPERATOR);

        group = new Group(1, "Laboratorio", LocalTime.of(8, 0),
                LocalTime.of(18, 0), "123456", "admin");
        group.addMember(username);
        group.addItem(new Item(1, "Trapano", 1, 4, 120));
        groupDAO = new InMemoryGroupDAO();
        groupDAO.save(group);
        bookingDAO = new InMemoryBookingDAO();
        clock = Clock.fixed(Instant.parse("2026-08-11T07:10:00Z"),
                ZoneId.of("Europe/Rome"));
        controller = new BookItemController(groupDAO, userDAO, bookingDAO,
                () -> operatorBean, clock);
    }

    @Test
    void exposesMembershipGroupsAndCreatesTheBooking() {
        assertEquals(1, controller.getMyGroups().size());

        BookingBean created = controller.createBooking(new BookingRequestBean(
                group.getGroupID(), 1, TODAY.plusDays(1), LocalTime.of(10, 0), 60));

        assertEquals("Trapano", created.getItemName());
        assertEquals(LocalTime.of(11, 0), created.getEndTime());
        assertEquals(1, bookingDAO.findAll().size());
    }

    @Test
    void revalidatesAvailabilityWhenCreating() {
        bookingDAO.save(new Booking("occupied", group.getGroupID(), 1, "other",
                TODAY.plusDays(1), LocalTime.of(10, 0), 60));

        assertThrows(IllegalStateException.class, () -> controller.createBooking(
                new BookingRequestBean(group.getGroupID(), 1, TODAY.plusDays(1),
                        LocalTime.of(10, 30), 30)));
        assertEquals(1, bookingDAO.findAll().size());
    }

    @Test
    void blockedOperatorCannotBook() {
        group.toggleMemberStatus(operatorBean.getUsername());

        assertEquals(1, controller.getMyGroups().size());
        assertFalse(controller.getMyGroups().getFirst().isActive());
        assertThrows(IllegalStateException.class, () -> controller.createBooking(
                new BookingRequestBean(group.getGroupID(), 1, TODAY.plusDays(1),
                        LocalTime.of(10, 0), 30)));
    }

    @Test
    void availabilitySnapshotAvoidsReadsWhenTheStartTimeChanges() {
        CountingBookingDAO countingDAO = new CountingBookingDAO();
        BookItemController countingController = new BookItemController(
                groupDAO, userDAO, countingDAO, () -> operatorBean, clock);

        BookingAvailabilityBean availability = countingController.getAvailability(
                group.getGroupID(), 1, TODAY.plusDays(1));

        assertEquals(1, countingDAO.getReadCount());
        availability.getDurationsFor(LocalTime.of(9, 0));
        availability.getDurationsFor(LocalTime.of(10, 0));
        availability.getDurationsFor(LocalTime.of(11, 0));
        assertEquals(1, countingDAO.getReadCount());

        countingController.createBooking(new BookingRequestBean(group.getGroupID(), 1,
                TODAY.plusDays(1), LocalTime.of(10, 0), 30));
        assertEquals(2, countingDAO.getReadCount());
    }

    private static final class CountingBookingDAO extends InMemoryBookingDAO {
        private int readCount;

        @Override
        public synchronized java.util.List<Booking> findAll() {
            readCount++;
            return super.findAll();
        }

        int getReadCount() {
            return readCount;
        }
    }
}
