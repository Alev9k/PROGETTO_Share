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
import model.session.SessionContext;
import exceptions.UnauthorizedOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
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
    private MutableSessionContext session;

    private static final class MutableSessionContext implements SessionContext {
        private UserBean currentUser;

        private MutableSessionContext(UserBean currentUser) {
            setCurrentUser(currentUser);
        }

        private void setCurrentUser(UserBean currentUser) {
            this.currentUser = Objects.requireNonNull(currentUser);
        }

        @Override
        public UserBean requireCurrentUser() {
            return currentUser;
        }
    }

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
        session = new MutableSessionContext(adminBean);

        createdGroup = new CreateGroupController(groupDAO, userDAO, session).createGroup(
                new GroupBean("Gruppo " + suffix, LocalTime.of(8, 0), LocalTime.of(18, 0)));
    }

    @Test
    void acceptedRequestAddsMemberAndNotifiesOperator() throws Exception {
        session.setCurrentUser(operatorBean);
        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO, session);
        MembershipRequestBean request = joinController.requestAccess(createdGroup.getAccessToken());

        AccessNotificationController notifications =
                new AccessNotificationController(userDAO, groupDAO, requestDAO, session);
        session.setCurrentUser(adminBean);
        assertEquals(1, notifications.countPendingForAdmin());

        ManageOperatorsController manageController = new ManageOperatorsController(
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO, bookingDAO, session);
        assertEquals(1, manageController.getPendingRequests().size());
        manageController.acceptRequest(request);

        assertTrue(groupDAO.findGroupById(createdGroup.getGroupId())
                .isActiveMember(operatorUsername));
        assertEquals(0, notifications.countPendingForAdmin());

        session.setCurrentUser(operatorBean);
        List<MembershipRequestBean> unread = notifications.getUnreadResults();
        assertEquals(1, unread.size());
        assertEquals(MembershipRequestStatus.ACCEPTED, unread.getFirst().getStatus());

        notifications.markAsRead(unread.getFirst());
        assertTrue(notifications.getUnreadResults().isEmpty());
        String accessToken = createdGroup.getAccessToken();
        assertThrows(IllegalStateException.class, () ->
                joinController.requestAccess(accessToken));
    }

    @Test
    void rejectedRequestDoesNotAddMemberAndNotifiesOperator() throws Exception {
        session.setCurrentUser(operatorBean);
        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO, session);
        MembershipRequestBean request = joinController.requestAccess(createdGroup.getAccessToken());

        session.setCurrentUser(adminBean);
        ManageOperatorsController manageController = new ManageOperatorsController(
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO, bookingDAO, session);
        manageController.rejectRequest(request);

        assertFalse(groupDAO.findGroupById(createdGroup.getGroupId())
                .hasMember(operatorUsername));

        session.setCurrentUser(operatorBean);
        List<MembershipRequestBean> unread = new AccessNotificationController(
                userDAO, groupDAO, requestDAO, session).getUnreadResults();
        assertEquals(1, unread.size());
        assertEquals(MembershipRequestStatus.REJECTED, unread.getFirst().getStatus());
    }

    @Test
    void invalidTokenDoesNotCreateRequest() {
        session.setCurrentUser(operatorBean);
        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO, session);

        assertThrows(IllegalArgumentException.class,
                () -> joinController.requestAccess("999999"));
        assertTrue(requestDAO.findAll().isEmpty());
    }

    @Test
    void malformedTokenDoesNotCreateRequest() {
        session.setCurrentUser(operatorBean);
        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO, session);

        assertThrows(IllegalArgumentException.class,
                () -> joinController.requestAccess("TOKEN-NON-VALIDO"));
        assertTrue(requestDAO.findAll().isEmpty());
    }

    @Test
    void adminCannotManageRequestsOfAnotherAdminsGroup() throws Exception {
        String otherAdminUsername = "other-admin-" + UUID.randomUUID();
        userDAO.save(new Admin(otherAdminUsername, "password"));
        UserBean otherAdmin = new UserBean(otherAdminUsername, Role.ADMIN);

        session.setCurrentUser(operatorBean);
        JoinGroupController joinController =
                new JoinGroupController(groupDAO, userDAO, requestDAO, session);
        MembershipRequestBean request = joinController.requestAccess(createdGroup.getAccessToken());
        ManageOperatorsController manageController = new ManageOperatorsController(
                createdGroup.getGroupId(), userDAO, groupDAO, requestDAO, bookingDAO, session);

        session.setCurrentUser(otherAdmin);
        assertThrows(UnauthorizedOperationException.class,
                () -> manageController.acceptRequest(request));
        session.setCurrentUser(adminBean);
        assertEquals(1, manageController.getPendingRequests().size());
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
        MutableSessionContext fileSession = new MutableSessionContext(fileAdminBean);
        GroupBean fileGroup = new CreateGroupController(
                fileGroupDAO, fileUserDAO, fileSession).createGroup(
                new GroupBean("Gruppo persistente", LocalTime.of(8, 0),
                        LocalTime.of(18, 0)));

        fileSession.setCurrentUser(fileOperatorBean);
        MembershipRequestBean request = new JoinGroupController(
                fileGroupDAO, fileUserDAO, fileRequestDAO, fileSession).requestAccess(
                fileGroup.getAccessToken());
        fileSession.setCurrentUser(fileAdminBean);
        new ManageOperatorsController(fileGroup.getGroupId(), fileUserDAO,
                fileGroupDAO, fileRequestDAO, new InMemoryBookingDAO(), fileSession)
                .acceptRequest(request);

        FileGroupDAO restartedGroupDAO = new FileGroupDAO(
                groupsFile, itemsFile, membershipsFile);
        Group reloaded = restartedGroupDAO.findGroupById(fileGroup.getGroupId());
        assertTrue(reloaded.isActiveMember(fileOperatorUsername));
        List<model.bean.OperatorBean> reloadedMembers = new ManageOperatorsController(
                fileGroup.getGroupId(), fileUserDAO, restartedGroupDAO, fileRequestDAO,
                new InMemoryBookingDAO(), fileSession)
                .getOperatorList();
        assertEquals(1, reloadedMembers.size());
        assertEquals(fileOperatorUsername, reloadedMembers.getFirst().getUsername());
    }
}
