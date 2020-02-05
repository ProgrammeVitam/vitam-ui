package fr.gouv.vitamui.iam.internal.server.owner.dao;

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
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = OwnerRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class OwnerRepositoryTest {

    @Autowired
    private OwnerRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testSave() {
        final Owner o = repository.save(IamServerUtilsTest.builOwner("id"));
        assertThat(o.getId()).isEqualTo("id");
    }
}
