package model.bean;

public class ItemBean {
    private String itemName;
    private String assetName;

    public ItemBean() {}
    public ItemBean(String name, String asset) { this.itemName = name; this.assetName = asset; }

    // Getters e Setters
    public String getItemName() { return itemName; }
    public void setItemName(String n) { this.itemName = n; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String a) { this.assetName = a; }
}