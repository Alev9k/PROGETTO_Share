package share.model.entity;

public class Technician {
    private String username;
    // Possiamo aggiungere una specializzazione o un ID se necessario in futuro

    public Technician(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
