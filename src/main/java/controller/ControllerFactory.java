package controller;

import model.dao.*;

public class ControllerFactory {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final MembershipRequestDAO membershipRequestDAO;
    private final BookingDAO bookingDAO;

    public ControllerFactory(UserDAO userDAO, GroupDAO groupDAO,
                             MembershipRequestDAO membershipRequestDAO,
                             BookingDAO bookingDAO) {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.membershipRequestDAO = membershipRequestDAO;
        this.bookingDAO = bookingDAO;
    }

    // Nuovi metodi per il FrontController
    public LoginController createLoginController() {
        return new LoginController(userDAO);
    }

    public RegistrationController createRegistrationController() {
        return new RegistrationController(userDAO);
    }

    public ManageGroupController createManageGroupController() {
        return new ManageGroupController(userDAO, groupDAO);
    }

    // Metodi per le sotto-boundary (già definiti)
    public ManageOperatorsController createManageOperatorsController(int groupID) {
        return new ManageOperatorsController(groupID, userDAO, groupDAO,
                membershipRequestDAO, bookingDAO);
    }

    public ManageItemsController createManageItemsController(int groupID) {
        return new ManageItemsController(groupID, groupDAO, userDAO);
    }

    public CreateGroupController createCreateGroupController() {
        return new CreateGroupController(groupDAO, userDAO);
    }

    public JoinGroupController createJoinGroupController() {
        return new JoinGroupController(groupDAO, userDAO, membershipRequestDAO);
    }

    public AccessNotificationController createAccessNotificationController() {
        return new AccessNotificationController(userDAO, groupDAO, membershipRequestDAO);
    }

    public BookItemController createBookItemController() {
        return new BookItemController(groupDAO, userDAO, bookingDAO);
    }

    public MyBookingsController createMyBookingsController() {
        return new MyBookingsController(userDAO, groupDAO, bookingDAO);
    }
}
