package controller;

import exceptions.DuplicateItemNameException;
import exceptions.UnauthorizedOperationException;
import model.bean.CreateItemBean;
import model.bean.ItemBean;
import model.bean.Role;
import model.bean.UserBean;
import model.dao.FileGroupDAO;
import model.dao.InMemoryGroupDAO;
import model.dao.InMemoryUserDAO;
import model.entity.Admin;
import model.entity.Group;
import model.entity.ItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManageItemsControllerTest {
    private InMemoryUserDAO userDAO;
    private InMemoryGroupDAO groupDAO;
    private UserBean adminBean;
    private Group group;

    @BeforeEach
    void setUp() {
        String adminUsername = "item-admin-" + UUID.randomUUID();
        Admin admin = new Admin(adminUsername, "password");
        group = new Group(1, "Laboratorio", LocalTime.of(8, 0),
                LocalTime.of(18, 0), "TOKEN", adminUsername);
        admin.addManagedGroup(group);

        userDAO = new InMemoryUserDAO();
        userDAO.save(admin);
        groupDAO = new InMemoryGroupDAO();
        groupDAO.save(group);
        adminBean = new UserBean(adminUsername, Role.ADMIN);
    }

    @Test
    void createsAnAvailableItemInTheSelectedGroup() throws Exception {
        ManageItemsController controller =
                new ManageItemsController(group.getGroupID(), groupDAO, userDAO);

        ItemBean created = controller.createItem(
                new CreateItemBean("Trapano", 4, 90), adminBean);

        assertEquals(1, created.getItemId());
        assertEquals("Trapano", created.getItemName());
        assertEquals(ItemStatus.AVAILABLE, created.getStatus());
        assertEquals(1, controller.getItemList(adminBean).size());
        assertEquals(1, groupDAO.findGroupById(group.getGroupID()).getItems().size());
    }

    @Test
    void rejectsCaseInsensitiveDuplicateNames() throws Exception {
        ManageItemsController controller =
                new ManageItemsController(group.getGroupID(), groupDAO, userDAO);
        controller.createItem(new CreateItemBean("Trapano", 3, 60), adminBean);

        assertThrows(DuplicateItemNameException.class, () ->
                controller.createItem(new CreateItemBean("  trapano  ", 2, 30), adminBean));
    }

    @Test
    void rejectsAnAdminWhoDoesNotOwnTheGroup() {
        String otherUsername = "other-admin-" + UUID.randomUUID();
        userDAO.save(new Admin(otherUsername, "password"));
        UserBean otherAdmin = new UserBean(otherUsername, Role.ADMIN);
        ManageItemsController controller =
                new ManageItemsController(group.getGroupID(), groupDAO, userDAO);

        assertThrows(UnauthorizedOperationException.class, () ->
                controller.createItem(new CreateItemBean("Trapano", 3, 60), otherAdmin));
    }

    @Test
    void rejectsMaximumUsageThatIsNotAMultipleOfHalfAnHour() {
        ManageItemsController controller =
                new ManageItemsController(group.getGroupID(), groupDAO, userDAO);

        assertThrows(IllegalArgumentException.class, () ->
                controller.createItem(new CreateItemBean("Trapano", 3, 45), adminBean));
    }

    @Test
    void persistsAndReloadsItemsFromFiles(@TempDir Path tempDirectory) throws Exception {
        Path groupsFile = tempDirectory.resolve("groups.csv");
        Path itemsFile = tempDirectory.resolve("items.csv");
        FileGroupDAO fileGroupDAO = new FileGroupDAO(groupsFile, itemsFile);
        fileGroupDAO.save(group);
        ManageItemsController controller =
                new ManageItemsController(group.getGroupID(), fileGroupDAO, userDAO);

        controller.createItem(
                new CreateItemBean("Trapano, professionale", 5, 120), adminBean);

        Group reloaded = new FileGroupDAO(groupsFile, itemsFile)
                .findGroupById(group.getGroupID());
        assertEquals(1, reloaded.getItems().size());
        assertEquals("Trapano, professionale", reloaded.getItems().getFirst().getName());
        assertEquals(ItemStatus.AVAILABLE, reloaded.getItems().getFirst().getStatus());
    }
}
