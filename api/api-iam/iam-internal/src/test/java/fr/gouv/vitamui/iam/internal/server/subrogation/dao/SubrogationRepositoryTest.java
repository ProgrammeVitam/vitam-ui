package fr.gouv.vitamui.iam.internal.server.subrogation.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;

/**
 * Tests for {@link UserRepository}
 *
 */

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = SubrogationRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class SubrogationRepositoryTest {

    @Autowired
    private SubrogationRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testSave() {
        Subrogation subro = new Subrogation();
        subro.setDate(new Date());
        subro.setStatus(SubrogationStatusEnum.CREATED);
        subro.setSuperUser("superuser@vitamui.com");
        subro.setSurrogate("surrogate@vitamui.com");
        subro = repository.save(subro);
        assertThat(subro.getSuperUser()).isEqualTo("superuser@vitamui.com");
    }

}
