package model.bean;

import java.time.LocalTime;

/** Gruppo visto dall'operatore, comprensivo dello stato della sua membership. */
public final class OperatorGroupBean {
    private final int groupId;
    private final String groupName;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final boolean active;

    public OperatorGroupBean(int groupId, String groupName, LocalTime openTime,
                             LocalTime closeTime, boolean active) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.active = active;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public boolean isActive() {
        return active;
    }

    public String getStatusLabel() {
        return active ? "Attivo" : "Bloccato";
    }
}
