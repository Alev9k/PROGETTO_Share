package model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un gruppo operativo all'interno del sistema Share.
 * Contiene l'elenco degli operatori e dei beni (Items) assegnati.
 */
public class Group {
    private int groupID;                 // Identificativo univoco del gruppo
    private String name;                // Nome del gruppo
    private List<Operator> operatorsList; // Lista degli operatori membri
    private List<Item> itemList;        // Lista dei beni del gruppo
    public Group(int groupID, String name) {
        this.groupID = groupID;
        this.name = name;
        this.operatorsList = new ArrayList<>();
        this.itemList = new ArrayList<>();
    }

    public List<Item> getItems() {
        return itemList; // Ritorna l'elenco dei beni
    }

    public List<Operator> getOperators() {
        return operatorsList; // Ritorna l'elenco degli operatori
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
}