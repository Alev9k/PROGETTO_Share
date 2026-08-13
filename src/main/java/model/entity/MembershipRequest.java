package model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Richiesta di accesso di un operatore a un gruppo. */
public class MembershipRequest {
    private final String requestId;
    private final int groupId;
    private final String operatorUsername;
    private final LocalDateTime createdAt;
    private MembershipRequestStatus status;
    private boolean resultRead;

    public MembershipRequest(String requestId, int groupId, String operatorUsername,
                             LocalDateTime createdAt) {
        this(requestId, groupId, operatorUsername, MembershipRequestStatus.PENDING,
                createdAt, false);
    }

    public MembershipRequest(String requestId, int groupId, String operatorUsername,
                             MembershipRequestStatus status, LocalDateTime createdAt,
                             boolean resultRead) {
        this.requestId = Objects.requireNonNull(requestId);
        this.groupId = groupId;
        this.operatorUsername = Objects.requireNonNull(operatorUsername);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.resultRead = resultRead;
    }

    public void accept() {
        decide(MembershipRequestStatus.ACCEPTED);
    }

    public void reject() {
        decide(MembershipRequestStatus.REJECTED);
    }

    private void decide(MembershipRequestStatus newStatus) {
        if (status != MembershipRequestStatus.PENDING) {
            throw new IllegalStateException("La richiesta è già stata elaborata.");
        }
        status = newStatus;
        resultRead = false;
    }

    public void markResultAsRead() {
        if (status == MembershipRequestStatus.PENDING) {
            throw new IllegalStateException("Una richiesta pendente non ha ancora un esito.");
        }
        resultRead = true;
    }

    public String getRequestId() { return requestId; }
    public int getGroupId() { return groupId; }
    public String getOperatorUsername() { return operatorUsername; }
    public MembershipRequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isResultRead() { return resultRead; }
}
