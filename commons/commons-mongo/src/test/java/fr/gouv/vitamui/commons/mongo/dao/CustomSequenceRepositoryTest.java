package fr.gouv.vitamui.commons.mongo.dao;

import fr.gouv.vitamui.commons.mongo.CustomSequencesConstants;
import fr.gouv.vitamui.commons.mongo.TestMongoConfig;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(
    basePackageClasses = CustomSequenceRepository.class,
    repositoryBaseClass = VitamUIRepositoryImpl.class
)
public class CustomSequenceRepositoryTest {

    @Autowired
    private CustomSequenceRepository repository;

    private static final String TEST_IDENTIFIER = "testIdentifier";

    @Test
    public void testSetup() {
        assertThat(repository).isNotNull();
    }

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testCreateSequence() {
        createSequence(TEST_IDENTIFIER);
    }

    private void createSequence(final String seqName) {
        CustomSequence customSequenceEntity = new CustomSequence();
        customSequenceEntity.setId("id");
        customSequenceEntity.setName(seqName);
        customSequenceEntity.setSequence(1);
        customSequenceEntity = repository.save(customSequenceEntity);
        assertThat(customSequenceEntity).isNotNull();
    }

    @Test
    public void testIncrementSequence() {
        createSequence(TEST_IDENTIFIER);
        Optional<CustomSequence> sequence = repository.incrementSequence(
            TEST_IDENTIFIER,
            CustomSequencesConstants.DEFAULT_SEQUENCE_INCREMENT_VALUE
        );
        assertThat(sequence.isPresent()).isTrue();
        assertThat(sequence.get().getSequence()).isEqualTo(2);
    }
}
