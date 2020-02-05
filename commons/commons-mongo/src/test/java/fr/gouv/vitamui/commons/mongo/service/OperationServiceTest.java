/*package fr.gouv.vitamui.commons.mongo.service;

import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.domain.OperationDto;
import fr.gouv.vitamui.commons.api.domain.VitamOperationDto;
import fr.gouv.vitamui.commons.api.enums.OperationStatus;
import fr.gouv.vitamui.commons.api.enums.OperationType;
import fr.gouv.vitamui.commons.mongo.config.MongoConverterConfiguration;
import fr.gouv.vitamui.commons.mongo.config.TestMongoConfig;
import fr.gouv.vitamui.commons.mongo.converter.OperationConverter;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.dao.OperationRepository;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class, MongoConverterConfiguration.class })
@EnableMongoRepositories(basePackageClasses = OperationRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class OperationServiceTest {

    @Autowired
    private final OperationRepository repository = null;

    @Autowired
    private OperationConverter operationConverter;

    private CustomSequenceRepository customSequenceRepository = null;

    private OperationService service = null;

    @Before
    public void setup() {
        customSequenceRepository = mock(CustomSequenceRepository.class);
        service = new OperationService(customSequenceRepository, repository, operationConverter);
        repository.deleteAll();
    }

    @Test
    public void testCreationVitamUIOperationOk() {

        List<OperationDto> results = service.getAll(Optional.empty());
        Assert.assertEquals(0, results.size());

        OperationDto operation = OperationFactory.createOperation("system", OperationType.VITAMUI);
        operation = service.create(operation);
        Assert.assertNotNull(operation);

        results = service.getAll(Optional.empty());
        Assert.assertEquals(1, results.size());

        operation = results.get(0);
        Assert.assertEquals(OperationStatus.WAITING, operation.getStatus());
        Assert.assertEquals(OperationType.VITAMUI, operation.getType());
        Assert.assertEquals("system", operation.getAuthor());
        Assert.assertTrue(StringUtils.isNotBlank(operation.getId()));
        Assert.assertNotNull(operation.getCreationDate());
        Assert.assertNotNull(operation.getLastModificationDate());
        Assert.assertNull(operation.getVitamOperation());
    }

    @Test
    public void testCreationVitamOperationOk() {

        List<OperationDto> results = service.getAll(Optional.empty());
        Assert.assertEquals(0, results.size());

        OperationDto operation = OperationFactory.createOperation("system", OperationType.VITAM);
        operation = service.create(operation);
        Assert.assertNotNull(operation);

        results = service.getAll(Optional.empty());
        Assert.assertEquals(1, results.size());

        operation = results.get(0);
        Assert.assertEquals(OperationStatus.WAITING, operation.getStatus());
        Assert.assertEquals(OperationType.VITAM, operation.getType());
        Assert.assertEquals("system", operation.getAuthor());
        Assert.assertTrue(StringUtils.isNotBlank(operation.getId()));
        Assert.assertNotNull(operation.getCreationDate());
        Assert.assertNotNull(operation.getLastModificationDate());
        Assert.assertNotNull(operation.getVitamOperation());
    }

    @Test
    public void testCreationWithUsefullInformation() {

        List<OperationDto> results = service.getAll(Optional.empty());
        Assert.assertEquals(0, results.size());

        OperationDto operation = OperationFactory.createOperation("system", OperationType.VITAMUI);
        operation.setVitamOperation(new VitamOperationDto());

        operation = service.create(operation);
        Assert.assertNotNull(operation);

        results = service.getAll(Optional.empty());
        Assert.assertEquals(1, results.size());

        operation = results.get(0);
        Assert.assertNull(operation.getVitamOperation());
    }

    @Test
    public void testCheckIntegrityWithValidationError() {

        final OperationDto operation = OperationFactory.createOperation("system", OperationType.VITAMUI);
        operation.setAuthor(null);

        try {
            service.checkIntegrity(operation);
            Assert.fail();
        }
        catch (final IllegalArgumentException exception) {
            Assert.assertEquals("Unable to validate the operation: author must not be blank", exception.getMessage());
        }
    }

    @Test
    public void testCheckIntegrityOnVitemOperationWithoutVitamOperation() {

        final OperationDto operation = OperationFactory.createOperation("system", OperationType.VITAM);
        operation.setVitamOperation(null);

        try {
            service.checkIntegrity(operation);
            Assert.fail();
        }
        catch (final IllegalArgumentException exception) {
            Assert.assertEquals("No Vitam operation has been set.", exception.getMessage());
        }
    }
}
*/
