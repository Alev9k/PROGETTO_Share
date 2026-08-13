package model.entity;

import model.observer.ItemBrokenEvent;
import model.observer.ItemSubject;

import java.time.LocalDateTime;
import java.util.Objects;

public class Item extends ItemSubject {
    private final int itemID;
    private final String name;
    private final int groupID;
    private int priority;
    private int maxUsageTime;
    private ItemStatus status;

    public Item(int itemID, String name, int groupID, int priority, int maxUsageTime) {
        this.itemID = itemID;
        this.name = name;
        this.groupID = groupID;
        this.priority = priority;
        this.maxUsageTime = maxUsageTime;
        this.status = ItemStatus.AVAILABLE;
    }

    public int getItemID() {
        return itemID;
    }

    public String getName() {
        return name;
    }

    public int getGroupID() {
        return groupID;
    }

    public int getPriority() {
        return priority;
    }

    public int getMaxUsageTime() {
        return maxUsageTime;
    }

    public ItemStatus getStatus() {
        return status;
    }

    /** Usato dai DAO durante la ricostruzione; il caso d'uso usa markAsBroken. */
    public void setStatus(ItemStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    /** Transizione di dominio che pubblica l'evento soltanto al primo guasto. */
    public void markAsBroken(String reportingOperator, LocalDateTime reportedAt) {
        if (status == ItemStatus.BROKEN) {
            throw new IllegalStateException("L'item è già stato segnalato come guasto.");
        }
        ItemBrokenEvent event = new ItemBrokenEvent(groupID, itemID, name,
                reportingOperator, reportedAt);
        status = ItemStatus.BROKEN;
        notifyItemBroken(event);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setMaxUsageTime(int maxUsageTime) {
        this.maxUsageTime = maxUsageTime;
    }
}
