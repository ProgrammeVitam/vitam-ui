package fr.gouv.vitamui.referential.internal.server.operation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.service.OperationService;
import fr.gouv.vitamui.referential.internal.server.service.ExternalParametersService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.mock;

class OperationInternalServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    AuditOptions auditOptions;
    String inputJsonConsistensy = "audit/AUDIT_FILE_CONSISTENCY.json";
    String inputJsonExistance = "audit/AUDIT_FILE_EXISTING.json";
    @Mock
    private OperationInternalService operationInternalService;
    @Mock
    private OperationService operationService;
    @Mock
    private LogbookService logbookService;
    @Mock
    private ExternalParametersService externalParametersService;
    public static final String DSL_QUERY_PROJECTION = "$projection";
    final private String AUDIT_FILE_CONSISTENCY = "AUDIT_FILE_CONSISTENCY";
    final private String AUDIT_FILE_EXISTING = "AUDIT_FILE_EXISTING";
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        operationService=mock(OperationService.class);
        logbookService=mock(LogbookService.class);
        externalParametersService=mock(ExternalParametersService.class);
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        operationInternalService = new OperationInternalService(operationService,logbookService,objectMapper,externalParametersService);
        auditOptions = new AuditOptions();
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }
    @Test
    void updateAuditDslQuery_should_handle_dsl_types() throws JsonProcessingException, FileNotFoundException {
        //AuditType ko
        auditOptions.setAuditType("fakeAuditType");
        String jsonDslQuery = PropertiesUtils.getResourceAsString("audit/AUDIT_FILE_CONSISTENCY.json").trim();
        JsonNode dslQuery = objectMapper.readTree(jsonDslQuery);
        auditOptions.setQuery(dslQuery);
        //set unexpected threshold
        assertThatCode(() -> operationInternalService.updateAuditDslQuery(auditOptions, Optional.empty()))
            .isInstanceOf(BadRequestException.class).hasMessageContaining("Invalid audit query");

        //set right AuditType ok
        auditOptions.setAuditType("dsl");
        auditOptions.setAuditActions(AUDIT_FILE_CONSISTENCY);
        //load query
        assertThatCode(() -> operationInternalService.updateAuditDslQuery(auditOptions, null))
            .isInstanceOf(BadRequestException.class).hasMessageContaining("Invalid audit query");

        //check expected threshold
        assertThatCode(() -> operationInternalService.updateAuditDslQuery(auditOptions, Optional.empty()))
        .doesNotThrowAnyException();


    }
    @Test
    void updateAuditDslQuery_should_handle_dsl_attributes() throws JsonProcessingException, FileNotFoundException {

        //check that dsl shoud not include projection
        String jsonWrongDslQuery = PropertiesUtils.getResourceAsString(inputJsonExistance).trim();
        JsonNode wrongDslQuery = objectMapper.readTree(jsonWrongDslQuery);
        auditOptions.setQuery(wrongDslQuery);
        auditOptions.setAuditType("dsl");
        auditOptions.setAuditActions(AUDIT_FILE_EXISTING); // or AUDIT_FILE_INTEGRITY
        operationInternalService.updateAuditDslQuery(auditOptions, Optional.of(10L));
        Assertions.assertFalse(containsAttribute(auditOptions.getQuery(), DSL_QUERY_PROJECTION));
        //check that dsl shoud include projection
        String jsonDslQuery = PropertiesUtils.getResourceAsString(inputJsonConsistensy).trim();
        JsonNode dslQuery = objectMapper.readTree(jsonDslQuery);
        auditOptions.setQuery(dslQuery);
        auditOptions.setAuditActions(AUDIT_FILE_CONSISTENCY); // or AUDIT_FILE_RECTIFICATION
        operationInternalService.updateAuditDslQuery(auditOptions, Optional.of(10L));
        Assertions.assertTrue(containsAttribute(auditOptions.getQuery(), DSL_QUERY_PROJECTION));
    }



    public boolean containsAttribute(JsonNode query, String attr){
        return query.findValue(attr) != null;
    }
}
