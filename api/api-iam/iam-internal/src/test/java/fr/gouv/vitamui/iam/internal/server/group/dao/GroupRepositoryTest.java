package fr.gouv.vitamui.iam.internal.server.group.dao;

import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * Tests for {@link UserRepository}
 *
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class GroupRepositoryTest extends AbstractMongoTests {

    @Autowired
    private GroupRepository repository;

    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testFindByProfileIds() {
        final Group groupAdmin = new Group();
        final String profileToFind = "adminFirstProfile";
        final String expectedId = "adminFirstProfile";
        groupAdmin.getProfileIds().add(profileToFind);
        groupAdmin.getProfileIds().add("adminSecondProfile");
        groupAdmin.setCustomerId("customerId");
        groupAdmin.setId(expectedId);
        groupAdmin.setName("name");
        groupAdmin.setDescription("description");
        groupAdmin.setIdentifier("102");
        repository.save(groupAdmin);
        final Group groupUser = new Group();
        groupUser.getProfileIds().add("userFirstProfile");
        groupUser.getProfileIds().add("userSecondProfile");
        groupUser.getProfileIds().add(profileToFind + "test");
        groupUser.setCustomerId("customerId");
        groupUser.setName("name");
        groupUser.setDescription("description");
        groupUser.setIdentifier("10245");
        repository.save(groupUser);

        final List<Group> result = repository.findByProfileIds(profileToFind);

        Assertions.assertNotNull(result, "ProfileGroup collection is null");
        Assertions.assertEquals(1, result.size(), "ProfileGroup collection incorrect size");
        Assertions.assertEquals(expectedId, result.get(0).getId(), "ProfileGroup incorrect id");
    }
}
