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

    public boolean canManageGroups() {
        return false;
    }

    public List<Group> getManagedGroups() {
        return List.of();
    }

    public void addManagedGroup(Group group) {
        throw new IllegalStateException("Questo utente non puo gestire gruppi.");
    }

    public void removeManagedGroup(int groupID) {
        throw new IllegalStateException("Questo utente non puo gestire gruppi.");
    }

    public void joinGroup(Group group) {
        throw new IllegalStateException("Questo utente non puo unirsi ai gruppi come operatore.");
    }
}
