package controller;

import model.bean.GroupBean;
import model.bean.MembershipRequestBean;
import model.bean.Role;
import model.bean.UserBean;
import model.dao.InMemoryGroupDAO;
import model.dao.InMemoryMembershipRequestDAO;
import model.dao.InMemoryUserDAO;
import model.entity.Admin;
import model.entity.MembershipRequestStatus;
import model.entity.Operator;
import exceptions.UnauthorizedOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupAccessWorkflowTest {
    private InMemoryUserDAO userDAO;
    private InMemoryGroupDAO groupDAO;
    private InMemoryMembershipRequestDAO requestDAO;
    private String adminUsername;
    private String operatorUsername;
    private UserBean adminBean;
    private UserBean operatorBean;
    private GroupBean createdGroup;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString();
        adminUsername = "admin-" + suffix;
        operatorUsername = "operator-" + suffix;
        userDAO = new InMemoryUserDAO();
        groupDAO = new InMemoryGroupDAO();
        requestDAO = new InMemoryMembershipRequestDAO();

        userDAO.save(new Admin(adminUsername, "password"));
        userDAO.save(new Operator(operatorUsername, "password"));
        adminBean = new UserBean(adminUsername, Role.ADMIN);
        operatorBean = new UserBean(operatorUsername, Role.OPERATOR);

        createdGroup = new CreateGroupController(groupDAO, userDAO).createGroup(
                new GroupBean("Gruppo " + suffix, LocalTime.of(8, 0), LocalTime.of(18, 0)),
                adminBean);
    }

    @Test
    void acceptedRequestAddsMemberAndNotifiesOperator() throws Exception {
        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO);
        MembershipRequestBean request = joinController.requestAccess(
                createdGroup.getAccessToken(), operatorBean);

        AccessNotificationController notifications =
                new AccessNotificationController(userDAO, groupDAO, requestDAO);
        assertEquals(1, notifications.countPendingForAdmin(adminBean));

        ManageOperatorsController manageController = new ManageOperatorsController(
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO);
        assertEquals(1, manageController.getPendingRequests(adminBean).size());
        manageController.acceptRequest(request, adminBean);

        assertNotNull(groupDAO.findGroupById(createdGroup.getGroupId())
                .findOperatorByUsername(operatorUsername));
        assertEquals(0, notifications.countPendingForAdmin(adminBean));

        List<MembershipRequestBean> unread = notifications.getUnreadResults(operatorBean);
        assertEquals(1, unread.size());
        assertEquals(MembershipRequestStatus.ACCEPTED, unread.getFirst().getStatus());

        notifications.markAsRead(unread.getFirst(), operatorBean);
        assertTrue(notifications.getUnreadResults(operatorBean).isEmpty());
        assertThrows(IllegalStateException.class, () ->
                joinController.requestAccess(createdGroup.getAccessToken(), operatorBean));
    }

    @Test
    void rejectedRequestDoesNotAddMemberAndNotifiesOperator() throws Exception {
        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO);
        MembershipRequestBean request = joinController.requestAccess(
                createdGroup.getAccessToken(), operatorBean);

        ManageOperatorsController manageController = new ManageOperatorsController(
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO);
        manageController.rejectRequest(request, adminBean);

        assertNull(groupDAO.findGroupById(createdGroup.getGroupId())
                .findOperatorByUsername(operatorUsername));

        List<MembershipRequestBean> unread = new AccessNotificationController(
                userDAO, groupDAO, requestDAO).getUnreadResults(operatorBean);
        assertEquals(1, unread.size());
        assertEquals(MembershipRequestStatus.REJECTED, unread.getFirst().getStatus());
    }

    @Test
    void invalidTokenDoesNotCreateRequest() {
        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO);

        assertThrows(IllegalArgumentException.class,
                () -> joinController.requestAccess("TOKEN-NON-VALIDO", operatorBean));
        assertTrue(requestDAO.findAll().isEmpty());
    }

    @Test
    void adminCannotManageRequestsOfAnotherAdminsGroup() throws Exception {
        String otherAdminUsername = "other-admin-" + UUID.randomUUID();
        userDAO.save(new Admin(otherAdminUsername, "password"));
        UserBean otherAdmin = new UserBean(otherAdminUsername, Role.ADMIN);

        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO);
        MembershipRequestBean request = joinController.requestAccess(
                createdGroup.getAccessToken(), operatorBean);
        ManageOperatorsController manageController = new ManageOperatorsController(
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO);

        assertThrows(UnauthorizedOperationException.class,
                () -> manageController.acceptRequest(request, otherAdmin));
        assertEquals(1, manageController.getPendingRequests(adminBean).size());
    }
}
