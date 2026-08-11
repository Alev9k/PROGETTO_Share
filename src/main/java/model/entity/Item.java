package model.entity;

import java.util.Objects;

public class Item {
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

    public void setStatus(ItemStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setMaxUsageTime(int maxUsageTime) {
        this.maxUsageTime = maxUsageTime;
    }
}
