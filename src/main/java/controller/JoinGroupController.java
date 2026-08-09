package controller;

import model.bean.MembershipRequestBean;
import model.bean.UserBean;
import model.dao.GroupDAO;
import model.dao.MembershipRequestDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.MembershipRequest;
import model.entity.Operator;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Controller applicativo del caso d'uso "Richiedi accesso al gruppo". */
public class JoinGroupController {
    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final MembershipRequestDAO requestDAO;

    public JoinGroupController(GroupDAO groupDAO, UserDAO userDAO,
                               MembershipRequestDAO requestDAO) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.requestDAO = requestDAO;
    }

    public MembershipRequestBean requestAccess(String accessToken, UserBean operatorBean) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Inserisci il token di accesso.");
        }
        if (operatorBean == null || operatorBean.getUsername() == null) {
            throw new IllegalArgumentException("Operatore non valido.");
        }

        if (!(userDAO.findByUsername(operatorBean.getUsername()) instanceof Operator operator)) {
            throw new IllegalArgumentException("Solo un operatore può richiedere l'accesso a un gruppo.");
        }

        Group group = groupDAO.findGroupByAccessToken(accessToken.trim());
        if (group == null) {
            throw new IllegalArgumentException("Token non valido: nessun gruppo trovato.");
        }
        if (group.findOperatorByUsername(operator.getUsername()) != null
                || requestDAO.hasAcceptedRequest(group.getGroupID(), operator.getUsername())) {
            throw new IllegalStateException("Sei già membro di questo gruppo.");
        }
        if (requestDAO.findPending(group.getGroupID(), operator.getUsername()) != null) {
            throw new IllegalStateException("Hai già una richiesta in attesa per questo gruppo.");
        }

        MembershipRequest request = new MembershipRequest(
                UUID.randomUUID().toString(), group.getGroupID(), operator.getUsername());
        requestDAO.save(request);
        return toBean(request, group);
    }

    public List<MembershipRequestBean> getRequestHistory(UserBean operatorBean) {
        if (operatorBean == null || operatorBean.getUsername() == null) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return requestDAO.findByOperatorUsername(operatorBean.getUsername()).stream()
                .sorted(Comparator.comparing(MembershipRequest::getCreatedAt).reversed())
                .map(request -> toBean(request, groupDAO.findGroupById(request.getGroupId())))
                .toList();
    }

    private MembershipRequestBean toBean(MembershipRequest request, Group group) {
        String groupName = group == null ? "Gruppo non disponibile" : group.getName();
        return new MembershipRequestBean(request.getRequestId(), request.getGroupId(),
                groupName, request.getOperatorUsername(), request.getStatus(),
                request.getCreatedAt());
    }
}
