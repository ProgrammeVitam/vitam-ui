package fr.gouv.vitamui.iam.internal.server.profile.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

/**
 * Tests for {@link UserRepository}
 *
 */

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = ProfileRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testSaveProfile() {
        final Profile p = repository.save(IamServerUtilsTest.buildProfile("id", "identifier", "name", "customerId", 10,
                CommonConstants.USERS_APPLICATIONS_NAME));
        assertThat(p.getId()).isEqualTo("id");
    }

    @Test
    public void testCheckExistByNameAndTenantId() {
        final Profile profilep = IamServerUtilsTest.buildProfile();
        repository.save(profilep);

        final Example<Profile> profile = Example.of(profilep,
                ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths("enabled"));
        final boolean exist = repository.exists(profile);

        assertThat(exist).isTrue();
    }

    @Test
    public void testFindByEnabledAndTenantIdIn() {
        final Profile p1 = IamServerUtilsTest.buildProfile("id", "identifier1", "name", "customerId", 10,
                CommonConstants.USERS_APPLICATIONS_NAME);
        final Profile p2 = IamServerUtilsTest.buildProfile("id2", "identifier2", "name", "customerId", 11,
                CommonConstants.USERS_APPLICATIONS_NAME);
        repository.save(p1);
        repository.save(p2);
        final List<Profile> res = repository.findByEnabledAndTenantIdentifierIn(true, Arrays.asList(10, 12));
        assertThat(res).isNotEmpty();
        assertThat(res.size()).isEqualTo(1);
    }

}
