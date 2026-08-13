package controller;

import model.bean.MembershipRequestBean;
import model.dao.GroupDAO;
import model.dao.MembershipRequestDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.MembershipRequest;
import model.entity.Operator;
import model.session.SessionContext;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Controller applicativo del caso d'uso "Richiedi accesso al gruppo". */
public class JoinGroupController {
    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final MembershipRequestDAO requestDAO;
    private final SessionContext session;
    private final Clock clock;

    public JoinGroupController(GroupDAO groupDAO, UserDAO userDAO,
                               MembershipRequestDAO requestDAO,
                               SessionContext session) {
        this(groupDAO, userDAO, requestDAO, session, Clock.systemDefaultZone());
    }

    JoinGroupController(GroupDAO groupDAO, UserDAO userDAO,
                        MembershipRequestDAO requestDAO, SessionContext session,
                        Clock clock) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.requestDAO = requestDAO;
        this.session = session;
        this.clock = clock;
    }

    public MembershipRequestBean requestAccess(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Inserisci il token di accesso.");
        }
        String normalizedToken = accessToken.trim();
        if (!normalizedToken.matches("\\d{6}")) {
            throw new IllegalArgumentException("Il token deve contenere esattamente 6 cifre.");
        }
        if (!(userDAO.findByUsername(session.requireCurrentUser().getUsername())
                instanceof Operator operator)) {
            throw new IllegalArgumentException("Solo un operatore può richiedere l'accesso a un gruppo.");
        }

        Group group = groupDAO.findGroupByAccessToken(normalizedToken);
        if (group == null) {
            throw new IllegalArgumentException("Token non valido: nessun gruppo trovato.");
        }
        if (group.hasMember(operator.getUsername())) {
            throw new IllegalStateException("Sei già membro di questo gruppo.");
        }
        if (requestDAO.findPending(group.getGroupID(), operator.getUsername()) != null) {
            throw new IllegalStateException("Hai già una richiesta in attesa per questo gruppo.");
        }

        MembershipRequest request = new MembershipRequest(
                UUID.randomUUID().toString(), group.getGroupID(), operator.getUsername(),
                LocalDateTime.now(clock));
        requestDAO.save(request);
        return toBean(request, group);
    }

    public List<MembershipRequestBean> getRequestHistory() {
        String operatorUsername = session.requireCurrentUser().getUsername();
        if (!(userDAO.findByUsername(operatorUsername) instanceof Operator)) {
            throw new IllegalArgumentException("Operatore non valido.");
        }
        return requestDAO.findByOperatorUsername(operatorUsername).stream()
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
