/*package fr.gouv.vitamui.commons.mongo.service;


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
