package share.model.dao;

import share.model.entity.User;
import java.util.ArrayList;
import java.util.List;

public class InMemoryUserDAO implements UserDAO {
    private static final List<User> userList = new ArrayList<>();

    @Override
    public void save(User user) {
        userList.add(user);
    }

    @Override
    public User findByUsername(String username) {
        return userList.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userList);
    }
}
