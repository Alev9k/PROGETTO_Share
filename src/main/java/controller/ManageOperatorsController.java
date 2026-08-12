package controller;

import exceptions.DAOException;
import exceptions.OperatorHasItemException;
import exceptions.UnauthorizedOperationException;
import model.bean.MembershipRequestBean;
import model.bean.OperatorBean;
import model.bean.UserBean;
import model.dao.BookingDAO;
import model.dao.GroupDAO;
import model.dao.MembershipRequestDAO;
import model.dao.UserDAO;
import model.entity.Booking;
import model.entity.Group;
import model.entity.GroupMembership;
import model.entity.MembershipRequest;
import model.entity.MembershipRequestStatus;
import model.entity.Operator;
import model.entity.User;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ManageOperatorsController {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final MembershipRequestDAO requestDAO;
    private final BookingDAO bookingDAO;
    private final Clock clock;
    private final Group contextGroup;

    public ManageOperatorsController(int groupID, UserDAO userDAO, GroupDAO groupDAO,
                                     MembershipRequestDAO requestDAO,
                                     BookingDAO bookingDAO) throws DAOException {
        this(groupID, userDAO, groupDAO, requestDAO, bookingDAO,
                Clock.systemDefaultZone());
    }

    ManageOperatorsController(int groupID, UserDAO userDAO, GroupDAO groupDAO,
                              MembershipRequestDAO requestDAO, BookingDAO bookingDAO,
                              Clock clock) throws DAOException {
        this.userDAO = Objects.requireNonNull(userDAO);
        this.groupDAO = Objects.requireNonNull(groupDAO);
        this.requestDAO = Objects.requireNonNull(requestDAO);
        this.bookingDAO = Objects.requireNonNull(bookingDAO);
        this.clock = Objects.requireNonNull(clock);
        this.contextGroup = groupDAO.findGroupById(groupID);

        if (this.contextGroup == null) {
            throw new DAOException("Gruppo non trovato per l'ID: " + groupID);
        }
    }

    public List<OperatorBean> getOperatorList() {
        return contextGroup.getMemberships().stream()
                .map(membership -> new OperatorBean(
                        membership.getOperatorUsername(), membership.isActive() ? 0 : 1))
                .toList();
    }

    public List<MembershipRequestBean> getPendingRequests(UserBean adminBean)
            throws UnauthorizedOperationException {
        requireAuthorizedAdmin(adminBean);
        return requestDAO.findByGroupId(contextGroup.getGroupID()).stream()
                .filter(request -> request.getStatus() == MembershipRequestStatus.PENDING)
                .map(this::toBean)
                .toList();
    }

    public void acceptRequest(MembershipRequestBean requestBean, UserBean adminBean)
            throws UnauthorizedOperationException {
        requireAuthorizedAdmin(adminBean);
        MembershipRequest request = requirePendingRequest(requestBean);
        User user = userDAO.findByUsername(request.getOperatorUsername());
        if (!(user instanceof Operator operator)) {
            throw new IllegalArgumentException("L'utente richiedente non è un operatore valido.");
        }

        if (!contextGroup.hasMember(operator.getUsername())) {
            contextGroup.addMember(operator.getUsername());
        }
        request.accept();

        groupDAO.update(contextGroup);
        requestDAO.update(request);
    }

    public void rejectRequest(MembershipRequestBean requestBean, UserBean adminBean)
            throws UnauthorizedOperationException {
        requireAuthorizedAdmin(adminBean);
        MembershipRequest request = requirePendingRequest(requestBean);
        request.reject();
        requestDAO.update(request);
    }

    public void toggleBlock(OperatorBean opBean) throws DAOException, OperatorHasItemException {
        if (opBean == null) {
            throw new IllegalArgumentException("Seleziona un operatore.");
        }
        GroupMembership membership = contextGroup.findMembership(opBean.getUsername());
        User user = userDAO.findByUsername(opBean.getUsername());
        if (membership == null || !(user instanceof Operator)) {
            throw new DAOException("Operatore non presente nel gruppo.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        List<Booking> operatorBookings = bookingDAO.findByOperatorAndGroup(
                opBean.getUsername(), contextGroup.getGroupID());

        if (membership.isActive()
                && operatorBookings.stream().anyMatch(booking -> booking.isInProgress(now))) {
            throw new OperatorHasItemException(
                    "L'operatore sta utilizzando un item e non può essere bloccato.");
        }

        if (membership.isActive()) {
            List<String> futureBookingIds = operatorBookings.stream()
                    .filter(booking -> booking.startsAfter(now))
                    .map(Booking::getBookingId)
                    .toList();
            bookingDAO.deleteByIds(futureBookingIds);
        }

        contextGroup.toggleMemberStatus(opBean.getUsername());
        groupDAO.update(contextGroup);
    }

    private MembershipRequest requirePendingRequest(MembershipRequestBean requestBean) {
        if (requestBean == null) {
            throw new IllegalArgumentException("Seleziona una richiesta.");
        }
        MembershipRequest request = requestDAO.findById(requestBean.getRequestId());
        if (request == null || request.getGroupId() != contextGroup.getGroupID()) {
            throw new IllegalArgumentException("Richiesta di accesso non trovata.");
        }
        if (request.getStatus() != MembershipRequestStatus.PENDING) {
            throw new IllegalStateException("La richiesta è già stata elaborata.");
        }
        return request;
    }

    private void requireAuthorizedAdmin(UserBean adminBean)
            throws UnauthorizedOperationException {
        User admin = adminBean == null ? null : userDAO.findByUsername(adminBean.getUsername());
        if (admin == null || !admin.canManageGroups()) {
            throw new UnauthorizedOperationException("Amministratore non autorizzato.");
        }

        boolean ownsGroup = contextGroup.isManagedBy(admin.getUsername())
                || admin.getManagedGroups().stream()
                .anyMatch(group -> group.getGroupID() == contextGroup.getGroupID());
        if (!ownsGroup) {
            throw new UnauthorizedOperationException(
                    "Non puoi gestire le richieste di questo gruppo.");
        }
    }

    private MembershipRequestBean toBean(MembershipRequest request) {
        return new MembershipRequestBean(request.getRequestId(), request.getGroupId(),
                contextGroup.getName(), request.getOperatorUsername(), request.getStatus(),
                request.getCreatedAt());
    }
}
