package fr.gouv.vitamui.iam.internal.server.application.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.application.domain.Application;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

/**
 * Tests for {@link UserRepository}
 *
 */

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = ApplicationRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testSaveApplication() {
        final Application app = repository.save(IamServerUtilsTest.buildApplication("identifier", "url"));
        assertThat(app.getIdentifier()).isEqualTo("identifier");
    }
}
