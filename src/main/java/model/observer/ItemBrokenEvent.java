package model.observer;

import java.time.LocalDateTime;

/** Dati immutabili pubblicati quando un item viene segnalato guasto. */
public record ItemBrokenEvent(int groupId, int itemId, String itemName,
                              String reportingOperator, LocalDateTime reportedAt) {
    public ItemBrokenEvent {
        if (groupId <= 0 || itemId <= 0) {
            throw new IllegalArgumentException("Gruppo e item dell'evento non sono validi.");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Il nome dell'item è obbligatorio.");
        }
        if (reportingOperator == null || reportingOperator.isBlank()) {
            throw new IllegalArgumentException("L'operatore segnalante è obbligatorio.");
        }
        if (reportedAt == null) {
            throw new IllegalArgumentException("La data della segnalazione è obbligatoria.");
        }
        itemName = itemName.trim();
        reportingOperator = reportingOperator.trim();
    }
}
