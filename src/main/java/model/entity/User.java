package model.entity;

import java.util.List;

public abstract class User {
    protected String username;
    protected String password;

    protected User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public abstract Role getRole();

    public abstract boolean canManageGroups();

    public abstract List<Group> getManagedGroups();

    public abstract void addManagedGroup(Group group);

}
