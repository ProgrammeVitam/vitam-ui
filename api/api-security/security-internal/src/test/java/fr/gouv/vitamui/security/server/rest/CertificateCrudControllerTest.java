package fr.gouv.vitamui.security.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.test.rest.AbstractCrudControllerTest;
import fr.gouv.vitamui.security.common.dto.CertificateDto;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.server.certificate.dao.CertificateRepository;
import fr.gouv.vitamui.security.server.certificate.domain.Certificate;
import fr.gouv.vitamui.security.server.certificate.service.CertificateCrudService;
import fr.gouv.vitamui.security.server.context.service.ContextService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public final class CertificateCrudControllerTest extends AbstractCrudControllerTest<CertificateDto, Certificate> {

    private static final String CONTEXT_ID = "contextId";

    @InjectMocks
    private CertificateCrudController controller;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private ContextService contextService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Override
    @BeforeEach
    public void setup() {
        super.setup();
        final CertificateCrudService service = new CertificateCrudService(
            sequenceGeneratorService,
            certificateRepository,
            contextService
        );
        controller.setCertificateCrudService(service);
    }

    @Test
    public void testCreationFailsAsTheContextMissesRoles() {
        try {
            prepareServices();
            when(contextService.getMany(CONTEXT_ID)).thenReturn(null);

            getController().create(buildDto());
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException | InvalidParseOperationException e) {
            Assertions.assertEquals("The context: " + CONTEXT_ID + " does not exist.", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsTheContextDoesNotExist() {
        try {
            final CertificateDto dto = buildDto();
            dto.setId(ID);
            when(contextService.getMany(CONTEXT_ID)).thenReturn(List.of(new ContextDto()));
            when(certificateRepository.existsById(any())).thenReturn(false);
            getController().update(ID, dto);
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException | InvalidParseOperationException e) {
            Assertions.assertEquals("Unable to update certificate: no entity found with id: " + ID, e.getMessage());
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

        when(contextService.getMany(CONTEXT_ID)).thenReturn(List.of(new ContextDto()));
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
