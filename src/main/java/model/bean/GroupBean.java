package model.bean;

public class GroupBean {
    private String groupName;
    private int groupId;

    public GroupBean(String name, int id) { this.groupName = name; this.groupId = id; }

    // Getters e Setters
    public String getGroupName() { return groupName; }
    public void setGroupName(String n) { this.groupName = n; }
    public int getGroupId() { return groupId; }
    public void setGroupId(int a) { this.groupId = a; }
}
