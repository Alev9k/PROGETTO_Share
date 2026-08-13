package controller;

import model.bean.MembershipRequestBean;
import model.dao.GroupDAO;
import model.dao.MembershipRequestDAO;
import model.entity.Group;
import model.entity.MembershipRequest;
import model.entity.MembershipRequestStatus;
import model.entity.Role;
import model.entity.User;
import model.session.SessionContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Controller applicativo per le notifiche derivate dalle richieste di accesso. */
public class AccessNotificationController {
    private final GroupDAO groupDAO;
    private final MembershipRequestDAO requestDAO;
    private final SessionContext session;

    public AccessNotificationController(GroupDAO groupDAO,
                                        MembershipRequestDAO requestDAO,
                                        SessionContext session) {
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
        User operator = requireOperator();
        return requestDAO.findByOperatorUsername(operator.getUsername()).stream()
                .filter(request -> request.getStatus() != MembershipRequestStatus.PENDING)
                .filter(request -> !request.isResultRead())
                .map(this::toBean)
                .toList();
    }

    public void markAsRead(MembershipRequestBean requestBean) {
        User operator = requireOperator();
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
        User user = session.requireCurrentUser();
        if (!user.canManageGroups()) {
            throw new IllegalArgumentException("Amministratore non autorizzato.");
        }
        return user;
    }

    private User requireOperator() {
        User user = session.requireCurrentUser();
        if (user.getRole() != Role.OPERATOR) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return user;
    }
}
