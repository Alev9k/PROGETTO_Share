package controller;

import controller.observer.AdminBrokenItemNotificationObserver;
import controller.observer.FutureBookingsCancellationObserver;
import model.dao.*;
import model.session.UserSession;

import java.util.List;

public class ControllerFactory {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final MembershipRequestDAO membershipRequestDAO;
    private final BookingDAO bookingDAO;
    private final NotificationDAO notificationDAO;
    private final UserSession userSession;

    public ControllerFactory(UserDAO userDAO, GroupDAO groupDAO,
                             MembershipRequestDAO membershipRequestDAO,
                             BookingDAO bookingDAO,
                             NotificationDAO notificationDAO,
                             UserSession userSession) {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.membershipRequestDAO = membershipRequestDAO;
        this.bookingDAO = bookingDAO;
        this.notificationDAO = notificationDAO;
        this.userSession = userSession;
    }

    // Nuovi metodi per il FrontController
    public LoginController createLoginController() {
        return new LoginController(userDAO, userSession);
    }

    public RegistrationController createRegistrationController() {
        return new RegistrationController(userDAO);
    }

    public ManageGroupController createManageGroupController() {
        return new ManageGroupController(userDAO, groupDAO, userSession);
    }

    // Metodi per le sotto-boundary (già definiti)
    public ManageOperatorsController createManageOperatorsController(int groupID) {
        return new ManageOperatorsController(groupID, userDAO, groupDAO,
                membershipRequestDAO, bookingDAO, userSession);
    }

    public ManageItemsController createManageItemsController(int groupID) {
        return new ManageItemsController(groupID, groupDAO, userDAO, userSession);
    }

    public CreateGroupController createCreateGroupController() {
        return new CreateGroupController(groupDAO, userDAO, userSession);
    }

    public JoinGroupController createJoinGroupController() {
        return new JoinGroupController(groupDAO, userDAO, membershipRequestDAO, userSession);
    }

    public AccessNotificationController createAccessNotificationController() {
        return new AccessNotificationController(
                userDAO, groupDAO, membershipRequestDAO, userSession);
    }

    public BookItemController createBookItemController() {
        return new BookItemController(groupDAO, userDAO, bookingDAO, userSession);
    }

    public MyBookingsController createMyBookingsController() {
        return new MyBookingsController(userDAO, groupDAO, bookingDAO, userSession);
    }

    public ReturnItemController createReturnItemController() {
        return new ReturnItemController(userDAO, groupDAO, bookingDAO, List.of(
                new AdminBrokenItemNotificationObserver(groupDAO, notificationDAO),
                new FutureBookingsCancellationObserver(
                        bookingDAO, groupDAO, notificationDAO)), userSession);
    }

    public EventNotificationController createEventNotificationController() {
        return new EventNotificationController(userDAO, notificationDAO, userSession);
    }
}
