package share.model.dao;

import share.model.entity.*;
import java.util.List;

public interface UserDAO {
    void save(User user);
    User findByUsername(String username);
    List<User> findAll();
}