package model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Notifica persistente destinata a un singolo utente. */
public class Notification {
    private final String notificationId;
    private final String recipientUsername;
    private final NotificationType type;
    private final String message;
    private final LocalDateTime createdAt;
    private boolean read;

    public Notification(String notificationId, String recipientUsername,
                        NotificationType type, String message,
                        LocalDateTime createdAt) {
        this(notificationId, recipientUsername, type, message, createdAt, false);
    }

    public Notification(String notificationId, String recipientUsername,
                        NotificationType type, String message,
                        LocalDateTime createdAt, boolean read) {
        if (notificationId == null || notificationId.isBlank()) {
            throw new IllegalArgumentException("L'identificativo della notifica è obbligatorio.");
        }
        if (recipientUsername == null || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Il destinatario della notifica è obbligatorio.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Il messaggio della notifica è obbligatorio.");
        }
        this.notificationId = notificationId;
        this.recipientUsername = recipientUsername.trim();
        this.type = Objects.requireNonNull(type, "Il tipo di notifica è obbligatorio.");
        this.message = message.trim();
        this.createdAt = Objects.requireNonNull(createdAt,
                "La data della notifica è obbligatoria.");
        this.read = read;
    }

    public String getNotificationId() { return notificationId; }
    public String getRecipientUsername() { return recipientUsername; }
    public NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }

    public void markAsRead() {
        read = true;
    }
}
