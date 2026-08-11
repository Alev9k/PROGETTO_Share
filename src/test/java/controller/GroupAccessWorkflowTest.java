package controller;

import model.bean.GroupBean;
import model.bean.MembershipRequestBean;
import model.bean.Role;
import model.bean.UserBean;
import model.dao.InMemoryGroupDAO;
import model.dao.InMemoryBookingDAO;
import model.dao.InMemoryMembershipRequestDAO;
import model.dao.InMemoryUserDAO;
import model.dao.FileGroupDAO;
import model.entity.Admin;
import model.entity.Group;
import model.entity.MembershipRequestStatus;
import model.entity.Operator;
import exceptions.UnauthorizedOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupAccessWorkflowTest {
    private InMemoryUserDAO userDAO;
    private InMemoryGroupDAO groupDAO;
    private InMemoryMembershipRequestDAO requestDAO;
    private InMemoryBookingDAO bookingDAO;
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
        bookingDAO = new InMemoryBookingDAO();

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
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO, bookingDAO);
        assertEquals(1, manageController.getPendingRequests(adminBean).size());
        manageController.acceptRequest(request, adminBean);

        assertTrue(groupDAO.findGroupById(createdGroup.getGroupId())
                .isActiveMember(operatorUsername));
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
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO, bookingDAO);
        manageController.rejectRequest(request, adminBean);

        assertFalse(groupDAO.findGroupById(createdGroup.getGroupId())
                .hasMember(operatorUsername));

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
                () -> joinController.requestAccess("999999", operatorBean));
        assertTrue(requestDAO.findAll().isEmpty());
    }

    @Test
    void malformedTokenDoesNotCreateRequest() {
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
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO, bookingDAO);

        assertThrows(UnauthorizedOperationException.class,
                () -> manageController.acceptRequest(request, otherAdmin));
        assertEquals(1, manageController.getPendingRequests(adminBean).size());
    }

    @Test
    void acceptedMembershipSurvivesGroupDaoRestart(@org.junit.jupiter.api.io.TempDir
                                                    Path tempDirectory) throws Exception {
        String suffix = UUID.randomUUID().toString();
        String fileAdminUsername = "file-admin-" + suffix;
        String fileOperatorUsername = "file-operator-" + suffix;
        InMemoryUserDAO fileUserDAO = new InMemoryUserDAO();
        InMemoryMembershipRequestDAO fileRequestDAO = new InMemoryMembershipRequestDAO();
        Path groupsFile = tempDirectory.resolve("groups.csv");
        Path itemsFile = tempDirectory.resolve("items.csv");
        Path membershipsFile = tempDirectory.resolve("memberships.csv");
        FileGroupDAO fileGroupDAO = new FileGroupDAO(
                groupsFile, itemsFile, membershipsFile);

        fileUserDAO.save(new Admin(fileAdminUsername, "password"));
        fileUserDAO.save(new Operator(fileOperatorUsername, "password"));
        UserBean fileAdminBean = new UserBean(fileAdminUsername, Role.ADMIN);
        UserBean fileOperatorBean = new UserBean(fileOperatorUsername, Role.OPERATOR);
        GroupBean fileGroup = new CreateGroupController(fileGroupDAO, fileUserDAO).createGroup(
                new GroupBean("Gruppo persistente", LocalTime.of(8, 0),
                        LocalTime.of(18, 0)), fileAdminBean);

        MembershipRequestBean request = new JoinGroupController(
                fileGroupDAO, fileUserDAO, fileRequestDAO).requestAccess(
                fileGroup.getAccessToken(), fileOperatorBean);
        new ManageOperatorsController(fileGroup.getGroupId(), fileUserDAO,
                fileGroupDAO, fileRequestDAO, new InMemoryBookingDAO())
                .acceptRequest(request, fileAdminBean);

        FileGroupDAO restartedGroupDAO = new FileGroupDAO(
                groupsFile, itemsFile, membershipsFile);
        Group reloaded = restartedGroupDAO.findGroupById(fileGroup.getGroupId());
        assertTrue(reloaded.isActiveMember(fileOperatorUsername));
        List<model.bean.OperatorBean> reloadedMembers = new ManageOperatorsController(
                fileGroup.getGroupId(), fileUserDAO, restartedGroupDAO, fileRequestDAO,
                new InMemoryBookingDAO())
                .getOperatorList();
        assertEquals(1, reloadedMembers.size());
        assertEquals(fileOperatorUsername, reloadedMembers.getFirst().getUsername());
    }
}
