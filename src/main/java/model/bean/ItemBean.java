package model.bean;

import model.entity.ItemStatus;

/** Rappresentazione in sola lettura di un item esposto alle boundary. */
public class ItemBean {
    private final int itemId;
    private final String itemName;
    private final int priority;
    private final int maxUsageTime;
    private final ItemStatus status;

    public ItemBean(int itemId, String itemName, int priority,
                    int maxUsageTime, ItemStatus status) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.priority = priority;
        this.maxUsageTime = maxUsageTime;
        this.status = status;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
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

    public String getStatusLabel() {
        return status.getLabel();
    }
}
