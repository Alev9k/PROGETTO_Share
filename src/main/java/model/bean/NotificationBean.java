package model.bean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationBean {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String notificationId;
    private final String typeLabel;
    private final String message;
    private final LocalDateTime createdAt;

    public NotificationBean(String notificationId, String typeLabel,
                            String message, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.typeLabel = typeLabel;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getNotificationId() { return notificationId; }
    public String getTypeLabel() { return typeLabel; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedAtLabel() { return DATE_TIME_FORMAT.format(createdAt); }
}
