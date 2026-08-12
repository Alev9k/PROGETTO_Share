package model.entity;

import java.util.Objects;

/** Associazione tra un gruppo e un operatore, parte dell'aggregato Group. */
public class GroupMembership {
    private final String operatorUsername;
    private MembershipStatus status;

    public GroupMembership(String operatorUsername) {
        this(operatorUsername, MembershipStatus.ACTIVE);
    }

    public GroupMembership(String operatorUsername, MembershipStatus status) {
        if (operatorUsername == null || operatorUsername.isBlank()) {
            throw new IllegalArgumentException("Lo username dell'operatore è obbligatorio.");
        }
        this.operatorUsername = operatorUsername.trim();
        this.status = Objects.requireNonNull(status, "Lo stato della membership è obbligatorio.");
    }

    public String getOperatorUsername() {
        return operatorUsername;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }

    void toggleStatus() {
        status = isActive() ? MembershipStatus.BLOCKED : MembershipStatus.ACTIVE;
    }
}
