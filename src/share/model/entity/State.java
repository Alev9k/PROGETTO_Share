package share.model.entity;

public class State {
    private int status; // 0: ATTIVO, 1: BLOCCATO
    private int groupID;

    public State(int groupID) {
        this.groupID = groupID;
        this.status = 0; // Di default l'utente entra come ATTIVO
    }

    public int getStatus() { return status; }
    public int getGroupID() { return groupID; }

    public void setStatus(int status) { this.status = status; }
}