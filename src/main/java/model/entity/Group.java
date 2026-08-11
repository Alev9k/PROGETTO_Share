package model.entity;

import exceptions.DuplicateItemNameException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un gruppo operativo all'interno del sistema Share.
 * Contiene l'elenco degli operatori e dei beni (Items) assegnati.
 */
public class Group {
    private final int groupID;                 // Identificativo univoco del gruppo
    private final String name;                // Nome del gruppo
    private LocalTime openTime;
    private LocalTime closeTime;
    private final String accessToken;
    private final String ownerUsername;
    private final List<GroupMembership> memberships;
    private final List<Item> itemList;        // Lista dei beni del gruppo
    public Group(int groupID, String name, LocalTime openTime, LocalTime closeTime) {
        this(groupID, name, openTime, closeTime, "", "");
    }

    public Group(int groupID, String name, LocalTime openTime, LocalTime closeTime,
                 String accessToken, String ownerUsername) {
        this.groupID = groupID;
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.accessToken = accessToken;
        this.ownerUsername = ownerUsername;
        this.memberships = new ArrayList<>();
        this.itemList = new ArrayList<>();
    }

    public List<Item> getItems() {
        return List.copyOf(itemList);
    }

    public Item getSingleItem(String name) {
        // Cicliamo l'elenco dei beni del gruppo
        for (Item item : itemList) {
            // Confrontiamo il nome (ignorando maiuscole/minuscole per sicurezza)
            if (item.getName().equalsIgnoreCase(name)) {
                return item; // Trovato!
            }
        }
        return null; // Se non esiste un bene con quel nome nel gruppo
    }

    public Item getSingleItemById(int itemID) {
        for (Item item : itemList) {
            if (item.getItemID() == itemID) {
                return item;
            }
        }
        return null;
    }

    public List<GroupMembership> getMemberships() {
        return List.copyOf(memberships);
    }

    public void addMember(String operatorUsername) {
        addMembership(new GroupMembership(operatorUsername));
    }

    public void addMembership(GroupMembership membership) {
        if (membership == null) {
            throw new IllegalArgumentException("La membership è obbligatoria.");
        }
        if (findMembership(membership.getOperatorUsername()) != null) {
            throw new IllegalStateException("L'operatore e gia presente nel gruppo.");
        }
        memberships.add(membership);
    }

    public GroupMembership findMembership(String operatorUsername) {
        if (operatorUsername == null) {
            return null;
        }
        return memberships.stream()
                .filter(membership -> membership.getOperatorUsername().equals(operatorUsername))
                .findFirst()
                .orElse(null);
    }

    public boolean hasMember(String operatorUsername) {
        return findMembership(operatorUsername) != null;
    }

    public boolean isActiveMember(String operatorUsername) {
        GroupMembership membership = findMembership(operatorUsername);
        return membership != null && membership.isActive();
    }

    public MembershipStatus toggleMemberStatus(String operatorUsername) {
        GroupMembership membership = findMembership(operatorUsername);
        if (membership == null) {
            throw new IllegalArgumentException("L'operatore non appartiene al gruppo.");
        }
        membership.toggleStatus();
        return membership.getStatus();
    }

    public void addItem(Item item) throws DuplicateItemNameException {
        if (item == null || item.getGroupID() != groupID) {
            throw new IllegalArgumentException("L'item non appartiene a questo gruppo.");
        }
        if (getSingleItem(item.getName()) != null) {
            throw new DuplicateItemNameException(
                    "Esiste già un item con questo nome nel gruppo.");
        }
        itemList.add(item);
    }

    // --- Getters aggiuntivi per la gestione ---

    public int getGroupID() {
        return groupID;
    }

    public String getName() {
        return name;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public boolean matchesAccessToken(String candidateToken) {
        return candidateToken != null
                && !accessToken.isBlank()
                && accessToken.equalsIgnoreCase(candidateToken.trim());
    }

    public boolean isManagedBy(String username) {
        return username != null && ownerUsername.equals(username);
    }
}
