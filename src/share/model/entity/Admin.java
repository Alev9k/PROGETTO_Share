package share.model.entity;

import java.util.ArrayList;
import java.util.List;

public class Admin {
    private String username;
    private List<Group> groupList; // Tutti i gruppi creati da questo Admin [cite: 76]
    private List<Asset> assetList; // Tutte le tipologie di asset (matrici) definite [cite: 77]

    public Admin(String username) {
        this.username = username;
        this.groupList = new ArrayList<>();
        this.assetList = new ArrayList<>();
    }

    // Metodi richiesti dal VOPC [cite: 78, 79]
    public List<Group> getGroups() {
        return groupList;
    }

    public List<Asset> getAssets() {
        return assetList;
    }

    public String getUsername() {
        return username;
    }
}