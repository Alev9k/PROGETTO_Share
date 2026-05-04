package model.dao;

import model.entity.*;
import java.util.List;

public interface UserDAO {
    void save(User user);
    User findByUsername(String username);
    List<User> findAll();
    void updateUser(User user);
}