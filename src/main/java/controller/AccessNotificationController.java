package controller;

import model.bean.MembershipRequestBean;
import model.bean.UserBean;
import model.dao.GroupDAO;
import model.dao.MembershipRequestDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.MembershipRequest;
import model.entity.MembershipRequestStatus;
import model.entity.Operator;
import model.entity.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Controller applicativo per le notifiche derivate dalle richieste di accesso. */
public class AccessNotificationController {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final MembershipRequestDAO requestDAO;

    public AccessNotificationController(UserDAO userDAO, GroupDAO groupDAO,
                                        MembershipRequestDAO requestDAO) {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.requestDAO = requestDAO;
    }

    public long countPendingForAdmin(UserBean adminBean) {
        User admin = requireAdmin(adminBean);
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

    public List<MembershipRequestBean> getUnreadResults(UserBean operatorBean) {
        Operator operator = requireOperator(operatorBean);
        return requestDAO.findByOperatorUsername(operator.getUsername()).stream()
                .filter(request -> request.getStatus() != MembershipRequestStatus.PENDING)
                .filter(request -> !request.isResultRead())
                .map(this::toBean)
                .toList();
    }

    public void markAsRead(MembershipRequestBean requestBean, UserBean operatorBean) {
        Operator operator = requireOperator(operatorBean);
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

    private User requireAdmin(UserBean bean) {
        User user = bean == null ? null : userDAO.findByUsername(bean.getUsername());
        if (user == null || !user.canManageGroups()) {
            throw new IllegalArgumentException("Amministratore non autorizzato.");
        }
        return user;
    }

    private Operator requireOperator(UserBean bean) {
        User user = bean == null ? null : userDAO.findByUsername(bean.getUsername());
        if (!(user instanceof Operator operator)) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return operator;
    }
}
