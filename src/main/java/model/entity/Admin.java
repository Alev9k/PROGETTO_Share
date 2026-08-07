package model.entity;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private List<Group> groupList; // Tutti i gruppi creati da questo model.factory.entity.Admin [cite: 76]

    public Admin(String username, String password) {
        super(username, password);
        this.groupList = new ArrayList<>();
    }

    @Override
    public boolean canManageGroups() {
        return true;
    }

    @Override
    public List<Group> getManagedGroups() {
        return List.copyOf(groupList);
    }

    @Override
    public void addManagedGroup(Group group) {
        groupList.add(group);
    }

    @Override
    public void removeManagedGroup(int groupID) {
        groupList.removeIf(group -> group.getGroupID() == groupID);
    }

    // Compatibilita con il codice esistente: usare preferibilmente getManagedGroups().
    public List<Group> getGroups() {
        return groupList;
    }
}
