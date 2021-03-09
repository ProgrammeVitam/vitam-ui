package fr.gouv.vitamui.iam.internal.server.group.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;

/**
 * Tests for {@link UserRepository}
 *
 */
@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = GroupRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class GroupRepositoryTest {

    @Autowired
    private GroupRepository repository;

    @After
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

        assertNotNull("ProfileGroup collection is null", result);
        assertEquals("ProfileGroup collection incorrect size", 1, result.size());
        assertEquals("ProfileGroup incorrect id", expectedId, result.get(0).getId());
    }
}
