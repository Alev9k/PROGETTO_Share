package share.controller;

import share.model.entity.*;
import java.util.List;

/**
 * Controller per la gestione dei gruppi, dei beni (Items) e dei membri (Operators).
 * Implementa la logica di business definita nel diagramma VOPC e nei casi d'uso.
 */
public class ManageGroupController {

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

    // --- Logica Gestione Item (Beni) ---

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
    public void removeItem(Item item, Group group) {
        // Verifica se l'Item è attualmente utilizzato (Step 9 della gestione Item)
        if (item.checkActiveness()) {
            // In un'applicazione reale, qui scatterebbe un messaggio di errore verso la UI
            return;
        }

        // Elimina il bene e invalida le sue prenotazioni (Step 10)
        group.removeItem(item);

        // La notifica agli Operator coinvolti verrà gestita tramite il Boundary
    }

    // --- Logica Gestione Membri (Operator) ---

    /**
     * Gestisce il cambio di stato (Attivo/Bloccato) di un operatore in un gruppo.
     */
    public void toggleOperatorState(Operator operator, Group group) {
        // Ora il metodo è disponibile nella classe Operator
        operator.toggleState(group.getGroupID());

        // Se l'operatore è stato appena bloccato, cancelliamo le sue prenotazioni
        if (!operator.checkActiveness(group.getGroupID())) {
            operator.cancelGroupBookings(group.getGroupID());
        }
    }
}