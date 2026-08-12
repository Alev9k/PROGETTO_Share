package controller;

import exceptions.OperatorHasItemException;
import model.bean.OperatorBean;
import model.dao.InMemoryBookingDAO;
import model.dao.InMemoryGroupDAO;
import model.dao.InMemoryMembershipRequestDAO;
import model.dao.InMemoryUserDAO;
import model.entity.Booking;
import model.entity.Group;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManageOperatorsBookingTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 11);

    private String username;
    private Group group;
    private InMemoryBookingDAO bookingDAO;
    private ManageOperatorsController controller;

    @BeforeEach
    void setUp() {
        username = "managed-operator-" + UUID.randomUUID();
        InMemoryUserDAO userDAO = new InMemoryUserDAO();
        userDAO.save(new Operator(username, "password"));
        group = new Group(1, "Laboratorio", LocalTime.of(8, 0),
                LocalTime.of(18, 0), "123456", "admin");
        group.addMember(username);
        InMemoryGroupDAO groupDAO = new InMemoryGroupDAO();
        groupDAO.save(group);
        bookingDAO = new InMemoryBookingDAO();
        controller = new ManageOperatorsController(group.getGroupID(), userDAO, groupDAO,
                new InMemoryMembershipRequestDAO(), bookingDAO,
                Clock.fixed(Instant.parse("2026-08-11T07:00:00Z"),
                        ZoneId.of("Europe/Rome")));
    }

    @Test
    void blockingOperatorDeletesFutureGroupBookings() throws Exception {
        Booking future = new Booking("future", 1, 1, username,
                TODAY, LocalTime.of(10, 0), 60);
        bookingDAO.save(future);

        controller.toggleBlock(new OperatorBean(username, 0));

        assertNull(bookingDAO.findById("future"));
        assertFalse(group.isActiveMember(username));
    }

    @Test
    void cannotBlockOperatorDuringCurrentUse() {
        bookingDAO.save(new Booking("current", 1, 1, username,
                TODAY, LocalTime.of(8, 30), 60));

        assertThrows(OperatorHasItemException.class,
                () -> controller.toggleBlock(new OperatorBean(username, 0)));
        assertTrue(group.isActiveMember(username));
        assertEquals("current", bookingDAO.findById("current").getBookingId());
    }
}
