package model.entity;

import java.util.List;

public class Operator extends User {
    public Operator(String username, String password) {
        super(username, password);
    }

    @Override
    public Role getRole() {
        return Role.OPERATOR;
    }

    @Override
    public boolean canManageGroups() {
        return false;
    }

    @Override
    public List<Group> getManagedGroups() {
        return List.of();
    }

    @Override
    public void addManagedGroup(Group group) {
        throw new IllegalStateException("Un operatore non puÃ² gestire gruppi.");
    }
}
