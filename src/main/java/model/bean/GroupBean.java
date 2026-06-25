package model.bean;

import java.time.LocalTime;

/**
 * Bean per il trasferimento dei dati del Gruppo tra Boundary e Controller.
 */
public class GroupBean {
    private int groupId;
    private String groupName;
    private LocalTime openTime;
    private LocalTime closeTime;

    // Costruttore completo (usato per la lettura)
    public GroupBean(int groupId, String groupName, LocalTime openTime, LocalTime closeTime) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    // Costruttore senza ID (usato dalla Boundary quando crea un nuovo gruppo)
    public GroupBean(String groupName, LocalTime openTime, LocalTime closeTime) {
        this.groupName = groupName;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.groupId = -1; // -1 indica che non è ancora stato assegnato dal DB/File
    }

    // Costruttore vecchio (se lo usi in ManageGroupController, tienilo per compatibilità)
    public GroupBean(String groupName, int groupId) {
        this.groupName = groupName;
        this.groupId = groupId;
    }

    public int getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public LocalTime getOpenTime() { return openTime; }
    public LocalTime getCloseTime() { return closeTime; }
}