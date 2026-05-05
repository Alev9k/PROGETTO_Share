package model.bean;

public class UserBean {

    private String username;
    private Role role;

    public  UserBean(String name, Role r){
        this.username = name;
        this.role = r;
    }

    public String getUsername() {return username;}
    public void setUsername(String username){
        this.username = username;
    }
    public Role getRole() {return role;}
    public void setRole(Role r){
        this.role = r;
    }
}
