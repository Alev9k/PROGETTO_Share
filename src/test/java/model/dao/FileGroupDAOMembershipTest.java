package model.dao;

import model.entity.Group;
import model.entity.MembershipStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileGroupDAOMembershipTest {

    @Test
    void persistsAndReloadsMembershipsAndTheirStatus(@TempDir Path tempDirectory) {
        Path groupsFile = tempDirectory.resolve("groups.csv");
        Path itemsFile = tempDirectory.resolve("items.csv");
        Path membershipsFile = tempDirectory.resolve("memberships.csv");
        FileGroupDAO groupDAO = new FileGroupDAO(
                groupsFile, itemsFile, membershipsFile);

        Group group = new Group(1, "Laboratorio", LocalTime.of(8, 0),
                LocalTime.of(18, 0), "123456", "admin");
        group.addMember("operator");
        groupDAO.save(group);

        Group reloaded = new FileGroupDAO(groupsFile, itemsFile, membershipsFile)
                .findGroupById(group.getGroupID());
        assertTrue(reloaded.isActiveMember("operator"));
        assertEquals(1, groupDAO.findGroupsByMemberUsername("operator").size());

        reloaded.toggleMemberStatus("operator");
        groupDAO.update(reloaded);

        FileGroupDAO restartedDAO = new FileGroupDAO(
                groupsFile, itemsFile, membershipsFile);
        Group blockedMembershipGroup = restartedDAO.findGroupById(group.getGroupID());
        assertFalse(blockedMembershipGroup.isActiveMember("operator"));
        assertEquals(MembershipStatus.BLOCKED,
                blockedMembershipGroup.findMembership("operator").getStatus());
        assertTrue(restartedDAO.findGroupsByActiveMemberUsername("operator").isEmpty());
    }
}
