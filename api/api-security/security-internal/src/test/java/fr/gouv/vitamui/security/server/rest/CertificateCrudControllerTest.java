package fr.gouv.vitamui.security.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.test.rest.AbstractCrudControllerTest;
import fr.gouv.vitamui.security.common.dto.CertificateDto;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.server.certificate.dao.CertificateRepository;
import fr.gouv.vitamui.security.server.certificate.domain.Certificate;
import fr.gouv.vitamui.security.server.certificate.service.CertificateCrudService;
import fr.gouv.vitamui.security.server.context.service.ContextService;

/**
 * Tests the {@link CertificateCrudController}.
 *
 *
 */
public final class CertificateCrudControllerTest extends AbstractCrudControllerTest<CertificateDto, Certificate> {

    private static final String CONTEXT_ID = "contextId";

    @InjectMocks
    private CertificateCrudController controller;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private ContextService contextService;

    @Mock
    private CustomSequenceRepository customSequenceRepository;

    @Override
    @Before
    public void setup() {
        super.setup();
        final CertificateCrudService service = new CertificateCrudService(customSequenceRepository, certificateRepository, contextService);
        controller.setCertificateCrudService(service);

    }

    @Test
    public void testCreationFailsAsTheContextMissesRoles() {
        try {
            prepareServices();
            when(contextService.getMany(CONTEXT_ID)).thenReturn(null);

            getController().create(buildDto());
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("The context: " + CONTEXT_ID + " does not exist.", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsTheContextDoesNotExist() {
        try {
            final CertificateDto dto = buildDto();
            dto.setId(ID);

//            prepareServices();
            when(contextService.getMany(CONTEXT_ID)).thenReturn(Arrays.asList(new ContextDto()));
            when(certificateRepository.existsById(any())).thenReturn(false);
//            when(contextService.getMany(CONTEXT_ID)).thenReturn(null);
//            when(certificateRepository.findById(ID)).thenReturn(Optional.empty());

            getController().update(ID, dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to update certificate: no entity found with id: " + ID, e.getMessage());
        }
    }

    @Override
    @Test
    public void testUpdateOK() throws Exception {
        final CertificateDto dto = buildDto();
        dto.setId(ID);
        prepareServices();
        getController().update(ID, dto);
    }

    @Override
    protected void prepareServices() {
        final Certificate entity = new Certificate();
        entity.setId(ID);

        when(contextService.getMany(CONTEXT_ID)).thenReturn(Arrays.asList(new ContextDto()));
        when(certificateRepository.findById(ID)).thenReturn(Optional.of(entity));
        when(certificateRepository.existsById(any())).thenReturn(true);
    }

    @Override
    protected CrudController<CertificateDto> getController() {
        return controller;
    }

    @Override
    protected CertificateDto buildDto() {
        final CertificateDto dto = new CertificateDto();
        dto.setSerialNumber("1234");
        dto.setContextId(CONTEXT_ID);
        dto.setIssuerDN("issuer");
        dto.setSubjectDN("subject");
        dto.setData("-- BEGIN CERT -- XXX --- END CERT ---");
        return dto;
    }


}
