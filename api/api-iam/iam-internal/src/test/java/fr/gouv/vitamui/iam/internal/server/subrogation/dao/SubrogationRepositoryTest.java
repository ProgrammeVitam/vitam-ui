package fr.gouv.vitamui.iam.internal.server.subrogation.dao;

import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UserRepository}
 */

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class SubrogationRepositoryTest extends AbstractMongoTests {

    @Autowired
    private SubrogationRepository repository;

    @AfterEach
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
