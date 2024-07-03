package fr.gouv.vitamui.commons.test.rest;

import fr.gouv.vitamui.commons.api.domain.BaseIdDocument;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public abstract class AbstractCrudControllerTest<D extends IdDto, E extends BaseIdDocument>
    extends AbstractMongoTests
    implements CrudControllerTest {

    protected static final String ID = "id";

    /**
     * {@inheritDoc}
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void testCreationOK() throws Exception {
        final D dto = buildDto();
        dto.setId(null);

        prepareServices();
        getController().create(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void testCreationFailsAsIdIsProvided() throws Exception {
        final D dto = buildDto();
        dto.setId(ID);

        prepareServices();
        try {
            getController().create(dto);
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException e) {
            Assertions.assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() throws Exception {
        final D dto = buildDto();
        dto.setId("anotherId");
        prepareServices();
        try {
            getController().update(ID, dto);
        } catch (final IllegalArgumentException e) {
            Assertions.assertEquals("The DTO identifier must match the path identifier for update.", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testUpdateOK() throws Exception {
        final D dto = buildDto();
        dto.setId(ID);
        prepareServices();
        getController().update(ID, dto);
    }

    protected abstract CrudController<D> getController();

    protected void prepareServices() {}

    protected abstract D buildDto() throws Exception;
}
