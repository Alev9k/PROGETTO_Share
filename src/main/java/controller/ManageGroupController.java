package controller;

import model.bean.*;
import model.entity.*;
import model.dao.*;
import exceptions.*;

import java.util.List;
/**
 * Controller per la gestione dei gruppi, dei beni (Items) e dei membri (Operators).
 * Implementa la logica di business definita nel diagramma VOPC e nei casi d'uso.
 */
public class ManageGroupController {
    private final UserDAO userDAO; // Per recuperare Admin/Operator
    private final FileGroupDAO groupDAO; // Per gestire i gruppi (Demo o VFS)

    public ManageGroupController(UserDAO uDao, FileGroupDAO gDao) {
        this.userDAO = uDao;
        this.groupDAO = gDao;
    }


    // --- Metodi di Recupero Dati ---

    public List<Group> getGroupList(Admin admin) {
        return admin.getGroups();
    }

    public List<Item> getItemList(Group group) {
        return group.getItems();
    }

    public List<Asset> getAssetList(Admin admin) {
        return admin.getAssets();
    }

    public List<Operator> getOperatorList(Group group) {
        return group.getOperators();
    }

    // --- Logica Gestione model.factory.entity.Item (Beni) ---

    /**
     * Verifica che il nome del nuovo bene sia univoco all'interno del gruppo.
     */
    public boolean checkItemName(String name, Group group) {
        // Requisito: inibire nomi duplicati nello stesso gruppo
        for (Item item : group.getItems()) {
            if (item.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        return true;
    }

    public void addItem(Item newItem, Group group) {
        group.addItem(newItem);
    }

    /**
     * Rimuove un bene dal gruppo solo se non è attualmente in uso.
     */
    public void removeItem(ItemBean itemBean, GroupBean groupBean)
            throws ItemInUseException {

        Group group = groupDAO.getGroupById(groupBean.getGroupId());
        Item item = group.getItemByName(itemBean.getItemName());

        // Step 9: Verifica se l'Item è in uso[cite: 1]
        if (item.checkActiveness()) {
            throw new ItemInUseException("Impossibile eliminare: l'oggetto è attualmente in uso.");
        }

        // Step 10: Elimina e invalida prenotazioni[cite: 1]
        group.removeItem(item);
        groupDAO.updateGroup(group);
    }

    // --- Logica Gestione Membri (model.factory.entity.Operator) ---

    /**
     * Gestisce il cambio di stato (Attivo/Bloccato) di un operatore in un gruppo.
     */
    public void toggleOperatorState(OperatorBean opBean, GroupBean gBean)
            throws OperatorHasItemException {

        Group group = groupDAO.getGroupById(gBean.getGroupId());
        Operator op = (Operator) userDAO.findByUsername(opBean.getUsername());

        // Step 9: Se vogliamo bloccare (0->1), verifichiamo che non abbia Item[cite: 1]
        if (op.checkActiveness(group.getGroupID()) && op.hasItemFromGroup(group.getGroupID())) {
            throw new OperatorHasItemException("L'operatore possiede un bene del gruppo e non può essere bloccato.");
        }

        // Step 10: Cambio stato e cancellazione prenotazioni[cite: 1]
        op.toggleState(group.getGroupID());
        if (!op.checkActiveness(group.getGroupID())) {
            op.cancelGroupBookings(group.getGroupID());
        }

        userDAO.updateUser(op);
    }
}