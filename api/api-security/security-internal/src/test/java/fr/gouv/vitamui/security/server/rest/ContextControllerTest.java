package fr.gouv.vitamui.security.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.test.rest.AbstractCrudControllerTest;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.server.certificate.dao.CertificateRepository;
import fr.gouv.vitamui.security.server.context.dao.ContextRepository;
import fr.gouv.vitamui.security.server.context.domain.Context;
import fr.gouv.vitamui.security.server.context.service.ContextService;

/**
 * Tests the {@link ContextController}.
 *
 *
 */
public final class ContextControllerTest extends AbstractCrudControllerTest<ContextDto, Context> {

    private static final String SERVICE = "badService";

    @InjectMocks
    private ContextController controller;

    @Mock
    private ContextRepository contextRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private CustomSequenceRepository customSequenceRepository;

    @Override
    @Before
    public void setup() {
        super.setup();
        final ContextService service = new ContextService(customSequenceRepository, contextRepository, certificateRepository);
        controller.setContextService(service);
    }

    @Test(expected = NullPointerException.class)
    public void testCreationFailsAsTheRoleIsNull() {
        final ContextDto dto = buildDto();
        List list = Arrays.asList(null) ;
        dto.setRoleNames(list);
        getController().create(dto);
        fail("should fail");
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateFailsAsTheRoleIsNull() {
        final ContextDto dto = buildDto();
        List list = Arrays.asList(null) ;
        dto.setRoleNames(list);
        dto.setId(ID);
        getController().update(ID, dto);
        fail("should fail");
    }

    @Test
    public void testCreationFailsAsTheRolesAreNotAllowed() {
        try {
            final ContextDto dto = buildDto();
            final Map<String, String[]> map = new HashMap<>();
            dto.setRoleNames(Arrays.asList("badRole"));
            getController().create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Some of the rolenames: [badRole] are not allowed", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsTheRolesAreNotAllowed() {
        try {
            final ContextDto dto = buildDto();
            dto.setRoleNames(Arrays.asList("badRole"));
            dto.setId(ID);

            final Context entity = new Context();
            entity.setId(ID);
            when(contextRepository.findById(ID)).thenReturn(Optional.of(entity));

            getController().update(ID, dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Some of the rolenames: [badRole] are not allowed", e.getMessage());
        }
    }

    @Override
    protected void prepareServices() {
        final Context entity = new Context();
        entity.setId(ID);
        when(contextRepository.findById(ID)).thenReturn(Optional.of(entity));

    }

    @Override
    protected ContextDto buildDto() {
        final ContextDto dto = new ContextDto();
        dto.setTenants(Arrays.asList(1));
        dto.setFullAccess(false);
        dto.setRoleNames(Arrays.asList(ServicesData.ROLE_GET_USERS));
        return dto;
    }

    @Override
    protected CrudController<ContextDto> getController() {
        return controller;
    }
}
