package fr.gouv.vitamui.referential.internal.server.operation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.service.OperationService;
import fr.gouv.vitamui.referential.internal.server.service.ExternalParametersService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;

class OperationInternalServiceTest {

    public static final String DSL_QUERY_PROJECTION = "$projection";
    private final String AUDIT_FILE_CONSISTENCY = "AUDIT_FILE_CONSISTENCY";

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OperationService operationService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private ExternalParametersService externalParametersService;

    @InjectMocks
    private OperationInternalService operationInternalService;

    private AuditOptions auditOptions;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        operationInternalService = new OperationInternalService(
            operationService,
            logbookService,
            objectMapper,
            externalParametersService
        );
        auditOptions = new AuditOptions();
    }

    @Test
    void updateAuditDslQuery_should_handle_dsl_types() throws JsonProcessingException, FileNotFoundException {
        // AuditType ko
        auditOptions.setAuditType("fakeAuditType");
        String jsonDslQuery = PropertiesUtils.getResourceAsString("audit/AUDIT_FILE_CONSISTENCY.json").trim();
        JsonNode dslQuery = objectMapper.readTree(jsonDslQuery);
        auditOptions.setQuery(dslQuery);
        // set unexpected threshold
        assertThatCode(() -> operationInternalService.updateAuditDslQuery(auditOptions, Optional.empty()))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid audit query");

        // set right AuditType ok
        auditOptions.setAuditType("dsl");
        auditOptions.setAuditActions(AUDIT_FILE_CONSISTENCY);
        // load query
        assertThatCode(() -> operationInternalService.updateAuditDslQuery(auditOptions, null))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid audit query");

        // check expected threshold
        assertThatCode(
            () -> operationInternalService.updateAuditDslQuery(auditOptions, Optional.empty())
        ).doesNotThrowAnyException();
    }

    @Test
    void updateAuditDslQuery_should_handle_dsl_attributes() throws JsonProcessingException, FileNotFoundException {
        // check that dsl should not include projection
        String inputJsonExistence = "audit/AUDIT_FILE_EXISTING.json";
        String jsonWrongDslQuery = PropertiesUtils.getResourceAsString(inputJsonExistence).trim();
        JsonNode wrongDslQuery = objectMapper.readTree(jsonWrongDslQuery);
        auditOptions.setQuery(wrongDslQuery);
        auditOptions.setAuditType("dsl");
        String AUDIT_FILE_EXISTING = "AUDIT_FILE_EXISTING";
        auditOptions.setAuditActions(AUDIT_FILE_EXISTING); // or AUDIT_FILE_INTEGRITY
        operationInternalService.updateAuditDslQuery(auditOptions, Optional.of(10L));
        Assertions.assertFalse(containsAttribute(auditOptions.getQuery(), DSL_QUERY_PROJECTION));

        // check that dsl should include projection
        String inputJsonConsistency = "audit/AUDIT_FILE_CONSISTENCY.json";
        String jsonDslQuery = PropertiesUtils.getResourceAsString(inputJsonConsistency).trim();
        JsonNode dslQuery = objectMapper.readTree(jsonDslQuery);
        auditOptions.setQuery(dslQuery);
        auditOptions.setAuditActions(AUDIT_FILE_CONSISTENCY); // or AUDIT_FILE_RECTIFICATION
        operationInternalService.updateAuditDslQuery(auditOptions, Optional.of(10L));
        Assertions.assertTrue(containsAttribute(auditOptions.getQuery(), DSL_QUERY_PROJECTION));
    }

    public boolean containsAttribute(JsonNode query, String attr) {
        return query.findValue(attr) != null;
    }
}
