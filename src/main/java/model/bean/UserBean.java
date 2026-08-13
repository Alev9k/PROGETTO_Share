package model.bean;

public class UserBean {

    private final String username;
    private final Role role;

    public  UserBean(String name, Role r){
        this.username = name;
        this.role = r;
    }

    public String getUsername() {return username;}
    public Role getRole() {return role;}
}
