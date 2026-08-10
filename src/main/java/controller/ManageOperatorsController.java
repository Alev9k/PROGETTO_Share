package controller;

import model.bean.OperatorBean;
import model.bean.MembershipRequestBean;
import model.bean.UserBean;
import model.dao.*;
import model.entity.*;
import exceptions.DAOException;
import exceptions.OperatorHasItemException;
import exceptions.UnauthorizedOperationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManageOperatorsController {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final MembershipRequestDAO requestDAO;
    private final Group contextGroup; // Il nostro attributo final per il contesto

    public ManageOperatorsController(int groupID, UserDAO userDAO, GroupDAO groupDAO,
                                     MembershipRequestDAO requestDAO) throws DAOException {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.requestDAO = requestDAO;
        // Recuperiamo il gruppo una sola volta all'inizio
        this.contextGroup = groupDAO.findGroupById(groupID);

        if (this.contextGroup == null) {
            throw new DAOException("Gruppo non trovato per l'ID: " + groupID);
        }
        hydrateAcceptedMembers();
    }

    public List<OperatorBean> getOperatorList() {
        Map<String, OperatorBean> members = new LinkedHashMap<>();
        // Usiamo l'attributo contextGroup invece di cercarlo nel DAO
        for (Operator op : contextGroup.getOperators()) {
            int status = op.checkActiveness(contextGroup.getGroupID()) ? 0 : 1;
            members.put(op.getUsername(), new OperatorBean(op.getUsername(), status));
        }

        // In modalità file l'esito accettato rappresenta anche l'associazione persistente.
        requestDAO.findByGroupId(contextGroup.getGroupID()).stream()
                .filter(request -> request.getStatus() == MembershipRequestStatus.ACCEPTED)
                .forEach(request -> members.putIfAbsent(request.getOperatorUsername(),
                        new OperatorBean(request.getOperatorUsername(), 0)));
        return new ArrayList<>(members.values());
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

        if (contextGroup.findOperatorByUsername(operator.getUsername()) == null) {
            operator.joinGroup(contextGroup);
        }
        request.accept();

        groupDAO.update(contextGroup);
        userDAO.updateUser(operator);
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
        Operator op = contextGroup.findOperatorByUsername(opBean.getUsername());
        if (op == null) {
            throw new DAOException("Operatore non presente nel gruppo.");
        }
        int gID = contextGroup.getGroupID();

        // Step 9: Verifica possesso Item
        if (op.checkActiveness(gID) && op.hasItemFromGroup(gID)) {
            throw new OperatorHasItemException("L'operatore possiede un bene e non può essere bloccato.");
        }

        op.toggleState(gID); // Step 10

        if (!op.checkActiveness(gID)) {
            List<Booking> removed = op.cancelGroupBookings(gID);
            for (Booking b : removed) {
                Item item = contextGroup.getSingleItem(b.getItemName());
                if (item != null) {
                    item.removeBooking(b);
                }
            }
        }

        // Persistenza
        userDAO.updateUser(op);
        groupDAO.update(contextGroup); // Salviamo le modifiche al gruppo
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

    private void hydrateAcceptedMembers() {
        requestDAO.findByGroupId(contextGroup.getGroupID()).stream()
                .filter(request -> request.getStatus() == MembershipRequestStatus.ACCEPTED)
                .forEach(request -> {
                    User user = userDAO.findByUsername(request.getOperatorUsername());
                    if (user instanceof Operator operator
                            && contextGroup.findOperatorByUsername(operator.getUsername()) == null) {
                        operator.joinGroup(contextGroup);
                    }
                });
    }
}
