package controller;

import model.bean.GroupBean;
import model.dao.InMemoryGroupDAO;
import model.dao.InMemoryUserDAO;
import model.entity.Admin;
import model.entity.Group;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManageGroupControllerTest {
    @Test
    void exposesTheAccessTokenOfManagedGroups() {
        InMemoryUserDAO userDAO = new InMemoryUserDAO();
        userDAO.save(new Admin("admin", "password"));
        InMemoryGroupDAO groupDAO = new InMemoryGroupDAO();
        groupDAO.save(new Group(1, "Laboratorio", LocalTime.of(8, 0),
                LocalTime.of(18, 0), "042731", "admin"));
        ManageGroupController controller = new ManageGroupController(userDAO, groupDAO);

        List<GroupBean> groups = controller.getGroupList("admin");

        assertEquals(1, groups.size());
        assertEquals("042731", groups.getFirst().getAccessToken());
    }
}
