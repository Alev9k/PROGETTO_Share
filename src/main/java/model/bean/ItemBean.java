package model.bean;

public class ItemBean {
    private final String itemName;
    private final int priority;
    private final int maxUsageTime;

    public ItemBean(String itemName, int priority, int maxUsageTime) {
        this.itemName = itemName;
        this.priority = priority;
        this.maxUsageTime = maxUsageTime;
    }

    public String getItemName() { return itemName; }
    public int getPriority() { return priority; }
    public int getMaxUsageTime() { return maxUsageTime; }
}
