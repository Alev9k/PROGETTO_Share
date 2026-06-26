package model.entity;

import java.util.ArrayList;
import java.util.List;

public class Asset {
    private final int assetID;     // ID univoco del catalogo
    private String name;           // Es. "Computer Portatile"
    private List<Question> report; // Domande dinamiche per la riconsegna

    public Asset(int assetID, String name) {
        this.assetID = assetID;
        this.name = name;
        this.report = new ArrayList<>();
    }

    public int getAssetID() {
        return assetID;
    }

    public String getName() {
        return name;
    }

    public List<Question> getReport() {
        return report;
    }

    public void addQuestion(Question q) {
        this.report.add(q);
    }
}