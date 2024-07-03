package fr.gouv.vitamui.commons.mongo.dao;

import fr.gouv.vitamui.commons.mongo.CustomSequencesConstants;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableMongoRepositories(basePackageClasses = PersonRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class CustomSequenceRepositoryTest extends AbstractMongoTests {

    private static final String TEST_IDENTIFIER = "testIdentifier";

    @Autowired
    private CustomSequenceRepository repository;

    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testSetup() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void testCreateSequence() {
        createSequence();
    }

    @Test
    public void testIncrementSequence() {
        createSequence();
        Optional<CustomSequence> sequence = repository.incrementSequence(
            TEST_IDENTIFIER,
            CustomSequencesConstants.DEFAULT_SEQUENCE_INCREMENT_VALUE
        );
        assertThat(sequence.isPresent()).isTrue();
        assertThat(sequence.get().getSequence()).isEqualTo(2);
    }

    private void createSequence() {
        CustomSequence customSequenceEntity = new CustomSequence();
        customSequenceEntity.setId("id");
        customSequenceEntity.setName(CustomSequenceRepositoryTest.TEST_IDENTIFIER);
        customSequenceEntity.setSequence(1);
        customSequenceEntity = repository.save(customSequenceEntity);
        assertThat(customSequenceEntity).isNotNull();
    }
}
