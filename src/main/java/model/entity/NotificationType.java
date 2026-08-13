package model.entity;

public enum NotificationType {
    ITEM_BROKEN("Item segnalato guasto"),
    BOOKING_CANCELLED("Prenotazione cancellata");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
