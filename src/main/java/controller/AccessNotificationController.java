package controller;

import model.bean.MembershipRequestBean;
import model.dao.GroupDAO;
import model.dao.MembershipRequestDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.MembershipRequest;
import model.entity.MembershipRequestStatus;
import model.entity.Operator;
import model.entity.User;
import model.session.SessionContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Controller applicativo per le notifiche derivate dalle richieste di accesso. */
public class AccessNotificationController {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final MembershipRequestDAO requestDAO;
    private final SessionContext session;

    public AccessNotificationController(UserDAO userDAO, GroupDAO groupDAO,
                                        MembershipRequestDAO requestDAO,
                                        SessionContext session) {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.requestDAO = requestDAO;
        this.session = session;
    }

    public long countPendingForAdmin() {
        User admin = requireAdmin();
        Set<Integer> groupIds = groupDAO.findGroupsByOwnerUsername(admin.getUsername()).stream()
                .map(Group::getGroupID)
                .collect(Collectors.toSet());
        if (groupIds.isEmpty()) {
            groupIds = admin.getManagedGroups().stream()
                    .map(Group::getGroupID)
                    .collect(Collectors.toSet());
        }
        Set<Integer> finalGroupIds = groupIds;
        return requestDAO.findAll().stream()
                .filter(request -> request.getStatus() == MembershipRequestStatus.PENDING)
                .filter(request -> finalGroupIds.contains(request.getGroupId()))
                .count();
    }

    public List<MembershipRequestBean> getUnreadResults() {
        Operator operator = requireOperator();
        return requestDAO.findByOperatorUsername(operator.getUsername()).stream()
                .filter(request -> request.getStatus() != MembershipRequestStatus.PENDING)
                .filter(request -> !request.isResultRead())
                .map(this::toBean)
                .toList();
    }

    public void markAsRead(MembershipRequestBean requestBean) {
        Operator operator = requireOperator();
        MembershipRequest request = requestDAO.findById(requestBean.getRequestId());
        if (request == null || !request.getOperatorUsername().equals(operator.getUsername())) {
            throw new IllegalArgumentException("Notifica non trovata.");
        }
        request.markResultAsRead();
        requestDAO.update(request);
    }

    private MembershipRequestBean toBean(MembershipRequest request) {
        Group group = groupDAO.findGroupById(request.getGroupId());
        String groupName = group == null ? "Gruppo non disponibile" : group.getName();
        return new MembershipRequestBean(request.getRequestId(), request.getGroupId(),
                groupName, request.getOperatorUsername(), request.getStatus(),
                request.getCreatedAt());
    }

    private User requireAdmin() {
        User user = userDAO.findByUsername(session.requireCurrentUser().getUsername());
        if (user == null || !user.canManageGroups()) {
            throw new IllegalArgumentException("Amministratore non autorizzato.");
        }
        return user;
    }

    private Operator requireOperator() {
        User user = userDAO.findByUsername(session.requireCurrentUser().getUsername());
        if (!(user instanceof Operator operator)) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return operator;
    }
}
