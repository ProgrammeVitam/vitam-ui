package fr.gouv.vitamui.referential.internal.server.operation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.service.OperationService;
import fr.gouv.vitamui.referential.common.service.VitamUIAccessContractService;
import fr.gouv.vitamui.referential.internal.server.accessionregister.AccessRegisterVitamQueryHelper;
import fr.gouv.vitamui.referential.internal.server.service.ExternalParametersService;
import org.json.JSONException;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.mock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

class OperationInternalServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    AuditOptions auditOptions;
    Optional<Long> thresholdOpt;
    String inputJsonConsistensy = "audit/AUDIT_FILE_CONSISTENCY.json";
    String inputJsonExistance = "audit/AUDIT_FILE_EXISTING.json";
    String inputJsonIntegrity = "audit/AUDIT_FILE_INTEGRITY.json";
    String inputJsonRectification = "audit/AUDIT_FILE_RECTIFICATION.json";
    @Mock
    private OperationInternalService operationInternalService;
    @Mock
    private OperationService operationService;
    @Mock
    private LogbookService logbookService;
    @Mock
    private ExternalParametersService externalParametersService;
    public static final String DSL_QUERY_PROJECTION = "$projection";
    public static final String DSL_QUERY_FILTER = "$filter";
    public static final String DSL_QUERY_FACETS = "$facets";
    final private String AUDIT_FILE_CONSISTENCY = "AUDIT_FILE_CONSISTENCY";
    final private String AUDIT_FILE_RECTIFICATION = "AUDIT_FILE_RECTIFICATION";
    final private String AUDIT_FILE_INTEGRITY = "AUDIT_FILE_INTEGRITY";
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
        assertThatCode(() -> {
            operationInternalService.updateAuditDslQuery(auditOptions, Optional.empty());
        }).isInstanceOf(BadRequestException.class).hasMessageContaining("Invalid audit query");

        //set right AuditType ok
        auditOptions.setAuditType("dsl");
        auditOptions.setAuditActions(AUDIT_FILE_CONSISTENCY);
        //load query
        assertThatCode(() -> {
            operationInternalService.updateAuditDslQuery(auditOptions, null);
        }).isInstanceOf(BadRequestException.class).hasMessageContaining("Invalid audit query");

        //check expected threshold
        assertThatCode(() -> {
            operationInternalService.updateAuditDslQuery(auditOptions, Optional.empty());
        }).doesNotThrowAnyException();


    }
    @Test
    void updateAuditDslQuery_should_handle_dsl_attributes() throws JsonProcessingException, FileNotFoundException {

        //check that dsl shoud not include projection
        String jsonWrongDslQuery = PropertiesUtils.getResourceAsString("audit/AUDIT_FILE_EXISTING.json").trim();
        JsonNode wrongDslQuery = objectMapper.readTree(jsonWrongDslQuery);
        auditOptions.setQuery(wrongDslQuery);
        auditOptions.setAuditType("dsl");
        auditOptions.setAuditActions(AUDIT_FILE_EXISTING); // or AUDIT_FILE_INTEGRITY
        operationInternalService.updateAuditDslQuery(auditOptions, Optional.of(10L));
        assertFalse(containsAttribute(auditOptions.getQuery(), DSL_QUERY_PROJECTION));
        //check that dsl shoud include projection
        String jsonDslQuery = PropertiesUtils.getResourceAsString("audit/AUDIT_FILE_CONSISTENCY.json").trim();
        JsonNode dslQuery = objectMapper.readTree(jsonDslQuery);
        auditOptions.setQuery(dslQuery);
        auditOptions.setAuditActions(AUDIT_FILE_CONSISTENCY); // or AUDIT_FILE_RECTIFICATION
        operationInternalService.updateAuditDslQuery(auditOptions, Optional.of(10L));
        assertTrue(containsAttribute(auditOptions.getQuery(), DSL_QUERY_PROJECTION));
    }



    public boolean containsAttribute(JsonNode query, String attr){
        if (query.findValue(attr) == null)
            return false;
        else
            return true;
    }
}
