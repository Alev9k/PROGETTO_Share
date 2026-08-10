package model.entity;

public enum ItemStatus {
    AVAILABLE("Disponibile"),
    IN_USE("In uso"),
    BROKEN("Guasto");

    private final String label;

    ItemStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
