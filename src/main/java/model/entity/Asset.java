package model.entity;

import java.util.ArrayList;
import java.util.List;

public class Asset {
    private String name;
    private int priority; // Scala 1-3
    private int maxUsageTime; // In minuti
    private List<Question> report; // Domande dinamiche

    public Asset(String name, int priority, int maxUsageTime) {
        this.name = name;
        this.priority = priority;
        this.maxUsageTime = maxUsageTime;
        this.report = new ArrayList<>();
    }

    // Getters
    public String getName() { return name; }
    public List<Question> getReport() { return report; }
    public int getMaxUsageTime() { return maxUsageTime; }

    public void addQuestion(Question q) {
        this.report.add(q);
    }
}