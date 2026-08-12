package controller;

import exceptions.UnauthorizedOperationException;
import model.bean.BookingBean;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyBookingsControllerTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 11);

    private InMemoryBookingDAO bookingDAO;
    private MyBookingsController controller;
    private UserBean operatorBean;

    @BeforeEach
    void setUp() throws Exception {
        InMemoryUserDAO userDAO = new InMemoryUserDAO();
        userDAO.save(new Operator("operator", "password"));
        userDAO.save(new Operator("other", "password"));
        operatorBean = new UserBean("operator", Role.OPERATOR);

        Group group = new Group(1, "Laboratorio", LocalTime.of(8, 0),
                LocalTime.of(18, 0), "123456", "admin");
        group.addMember("operator");
        group.addItem(new Item(1, "Trapano", 1, 4, 120));
        InMemoryGroupDAO groupDAO = new InMemoryGroupDAO();
        groupDAO.save(group);

        bookingDAO = new InMemoryBookingDAO();
        Clock clock = Clock.fixed(Instant.parse("2026-08-11T07:00:00Z"),
                ZoneId.of("Europe/Rome"));
        controller = new MyBookingsController(userDAO, groupDAO, bookingDAO, clock);
    }

    @Test
    void showsOnlyTheAuthenticatedOperatorsBookings() {
        bookingDAO.save(booking("future", "operator", LocalTime.of(10, 0)));
        bookingDAO.save(booking("other", "other", LocalTime.of(12, 0)));

        List<BookingBean> bookings = controller.getMyBookings(operatorBean);

        assertEquals(1, bookings.size());
        assertEquals("Laboratorio", bookings.getFirst().getGroupName());
        assertEquals("Trapano", bookings.getFirst().getItemName());
        assertTrue(bookings.getFirst().isDeletable());
    }

    @Test
    void physicallyDeletesOnlyAFutureOwnedBooking() throws Exception {
        bookingDAO.save(booking("future", "operator", LocalTime.of(10, 0)));

        controller.deleteBooking("future", operatorBean);

        assertNull(bookingDAO.findById("future"));
    }

    @Test
    void rejectsDeletionAfterTheBookingHasStarted() {
        bookingDAO.save(booking("current", "operator", LocalTime.of(8, 30)));

        assertFalse(controller.getMyBookings(operatorBean).getFirst().isDeletable());
        assertThrows(IllegalStateException.class,
                () -> controller.deleteBooking("current", operatorBean));
        assertEquals("current", bookingDAO.findById("current").getBookingId());
    }

    @Test
    void rejectsDeletionOfAnotherOperatorsBooking() {
        bookingDAO.save(booking("other", "other", LocalTime.of(10, 0)));

        assertThrows(UnauthorizedOperationException.class,
                () -> controller.deleteBooking("other", operatorBean));
    }

    private Booking booking(String id, String username, LocalTime start) {
        return new Booking(id, 1, 1, username, TODAY, start, 60);
    }
}
