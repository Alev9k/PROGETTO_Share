package model.dao;

import model.entity.Group;
import exceptions.DAOException;
import java.util.List;

public interface GroupDAO {
    List<Group> findAll() throws DAOException;
    void save(Group group) throws DAOException;
    void update(Group group) throws DAOException;
}