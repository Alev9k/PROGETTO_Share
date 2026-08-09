package model.entity;

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
    private List<Operator> operatorsList; // Lista degli operatori membri
    private List<Item> itemList;        // Lista dei beni del gruppo
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
        this.operatorsList = new ArrayList<>();
        this.itemList = new ArrayList<>();
    }

    public List<Item> getItems() {
        return itemList; // Ritorna l'elenco dei beni
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

    public List<Operator> getOperators() {
        return List.copyOf(operatorsList);
    }

    public void addOperator(Operator operator) {
        if (findOperatorByUsername(operator.getUsername()) != null) {
            throw new IllegalStateException("L'operatore e gia presente nel gruppo.");
        }
        operatorsList.add(operator);
    }

    public Operator findOperatorByUsername(String username) {
        return operatorsList.stream()
                .filter(operator -> operator.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public void addItem(Item item) {
        this.itemList.add(item); // Aggiunge un bene al gruppo
    }

    public void removeItem(Item item) {
        this.itemList.remove(item); // Rimuove un bene dal gruppo
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
