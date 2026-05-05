package model.bean;

public class OperatorBean {

    private String username;
    private int status;

    public OperatorBean(String name, int s) {
        this.username = name;
        this.status = s;
    }

    // Getters e Setters

    public String getUsername() { return username; }
    public void setUsername(String n) { this.username = n; }
    public int getStatus() { return status; }
    public void setStatus(int s) { this.status = s; }
}
