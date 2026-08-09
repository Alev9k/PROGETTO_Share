package model.bean;

import model.entity.MembershipRequestStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Bean usato dalle boundary per mostrare e selezionare una richiesta. */
public class MembershipRequestBean {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String requestId;
    private final int groupId;
    private final String groupName;
    private final String operatorUsername;
    private final MembershipRequestStatus status;
    private final LocalDateTime createdAt;

    public MembershipRequestBean(String requestId, int groupId, String groupName,
                                 String operatorUsername, MembershipRequestStatus status,
                                 LocalDateTime createdAt) {
        this.requestId = requestId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.operatorUsername = operatorUsername;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getRequestId() { return requestId; }
    public int getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public String getOperatorUsername() { return operatorUsername; }
    public MembershipRequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getStatusLabel() {
        return switch (status) {
            case PENDING -> "In attesa";
            case ACCEPTED -> "Accettata";
            case REJECTED -> "Rifiutata";
        };
    }

    public String getCreatedAtLabel() {
        return createdAt.format(DATE_FORMAT);
    }
}
