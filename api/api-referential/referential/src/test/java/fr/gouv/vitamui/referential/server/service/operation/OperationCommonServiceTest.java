package fr.gouv.vitamui.referential.server.service.operation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.model.AuditCreateOptions;
import fr.gouv.vitamui.referential.common.service.OperationCommonService;
import fr.gouv.vitamui.referential.server.service.probativevalue.ProbativeValueService;
import fr.gouv.vitamui.referential.server.service.service.ExternalParametersService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;

class OperationCommonServiceTest {

    public static final String DSL_QUERY_PROJECTION = "$projection";
    private final String AUDIT_FILE_CONSISTENCY = "AUDIT_FILE_CONSISTENCY";
    private final String AUDIT_PERIMETER_ORIGINATING_AGENCY = "AUDIT_PERIMETER_ORIGINATING_AGENCY";

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OperationCommonService operationCommonService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private ExternalParametersService externalParametersService;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProbativeValueService probativeValueService;

    @InjectMocks
    private OperationService operationService;

    private AuditCreateOptions auditCreateOptions;
    private AuditOptions auditOptions;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        operationService = new OperationService(
            operationCommonService,
            logbookService,
            objectMapper,
            externalParametersService,
            securityService,
            probativeValueService
        );
        auditCreateOptions = new AuditCreateOptions();
        auditOptions = new AuditOptions();
    }

    @Test
    void updateAuditDslQuery_should_handle_dsl_types() throws JsonProcessingException, FileNotFoundException {
        VitamContext vitamContext = new VitamContext(1);
        // AuditType ko
        auditCreateOptions.setAuditType("fakeAuditType");
        auditCreateOptions.setIngestOperationIds(new String[] { "fakeIngestOperationId1", "fakeIngestOperationId2" });
        auditCreateOptions.setOriginatingAgencyIds(
            new String[] { "fakeOriginatingAgencyId1", "fakeOriginatingAgencyId2" }
        );
        auditCreateOptions.setAttachmentPositionIds(
            new String[] { "fakeAttachmentPositionId1", "fakeAttachmentPositionId2" }
        );
        auditCreateOptions.setAuditPerimeter(AUDIT_PERIMETER_ORIGINATING_AGENCY);
        auditCreateOptions.setAuditActions(AUDIT_FILE_CONSISTENCY); // or AUDIT_FILE_RECTIFICATION
        // set unexpected threshold
        assertThatCode(() -> operationService.updateAuditDslQuery(auditCreateOptions, null, vitamContext))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid audit query");

        // set right AuditType ok
        auditCreateOptions.setAuditType("dsl");
        String AUDIT_FILE_EXISTING = "AUDIT_FILE_EXISTING";
        auditCreateOptions.setAuditActions(AUDIT_FILE_EXISTING); // or AUDIT_FILE_INTEGRITY
        // load query
        assertThatCode(() -> operationService.updateAuditDslQuery(auditCreateOptions, null, vitamContext))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid audit query");

        // check expected threshold
        assertThatCode(
            () -> operationService.updateAuditDslQuery(auditCreateOptions, Optional.empty(), vitamContext)
        ).doesNotThrowAnyException();
    }

    @Test
    void updateAuditDslQuery_should_handle_dsl_attributes() throws JsonProcessingException, FileNotFoundException {
        VitamContext vitamContext = new VitamContext(1);
        // check that dsl should not include projection
        auditCreateOptions.setAuditType("dsl");
        auditCreateOptions.setIngestOperationIds(new String[] { "fakeIngestOperationId1", "fakeIngestOperationId2" });
        auditCreateOptions.setOriginatingAgencyIds(
            new String[] { "fakeOriginatingAgencyId1", "fakeOriginatingAgencyId2" }
        );
        auditCreateOptions.setAttachmentPositionIds(
            new String[] { "fakeAttachmentPositionId1", "fakeAttachmentPositionId2" }
        );
        auditCreateOptions.setAuditPerimeter(AUDIT_PERIMETER_ORIGINATING_AGENCY);
        String AUDIT_FILE_EXISTING = "AUDIT_FILE_EXISTING";
        auditCreateOptions.setAuditActions(AUDIT_FILE_EXISTING); // or AUDIT_FILE_INTEGRITY
        auditOptions = operationService.updateAuditDslQuery(auditCreateOptions, Optional.of(10L), vitamContext);
        Assertions.assertFalse(containsAttribute(auditOptions.getQuery(), DSL_QUERY_PROJECTION));

        // check that dsl should include projection
        auditCreateOptions.setAuditActions(AUDIT_FILE_CONSISTENCY); // or AUDIT_FILE_RECTIFICATION
        auditOptions = operationService.updateAuditDslQuery(auditCreateOptions, Optional.of(10L), vitamContext);
        Assertions.assertTrue(containsAttribute(auditOptions.getQuery(), DSL_QUERY_PROJECTION));
    }

    public boolean containsAttribute(JsonNode query, String attr) {
        return query.findValue(attr) != null;
    }
}
