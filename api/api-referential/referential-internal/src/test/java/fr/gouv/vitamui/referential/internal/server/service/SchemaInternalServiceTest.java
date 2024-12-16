package fr.gouv.vitamui.referential.internal.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.service.ImportSchemaService;
import fr.gouv.vitamui.referential.internal.server.schema.ImportSchemaConverter;
import fr.gouv.vitamui.referential.internal.server.schema.SchemaInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SchemaInternalServiceTest {

    @Mock
    private ImportSchemaService importSchemaService;

    @Mock
    private ImportSchemaConverter converter;

    @Mock
    private LogbookService logbookService;

    @Mock
    private AdminExternalClient adminExternalClient;

    @Mock
    private ApplicationInternalRestClient applicationInternalRestClient;

    @Mock
    private InternalSecurityService internalSecurityService;

    @InjectMocks
    private SchemaInternalService schemaInternalService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = mock(ImportSchemaConverter.class);
        schemaInternalService = new SchemaInternalService(
            internalSecurityService,
            adminExternalClient,
            importSchemaService,
            objectMapper,
            converter,
            applicationInternalRestClient
        );
    }

    @Test
    @DisplayName("Import should return OK when schema is valid")
    public void importShouldReturnOk()
        throws IOException, AccessExternalClientException, InvalidParseOperationException {
        // Given
        VitamContext vitamContext = new VitamContext(0);
        String fileName = "import_schema_valid.csv";
        MultipartFile multipartFile = new MockMultipartFile(
            fileName,
            fileName,
            "text/csv",
            getClass().getResourceAsStream("/data/" + fileName)
        );

        when(internalSecurityService.getHttpContext()).thenReturn(
            new InternalHttpContext(0, "", "", "", "", "", "", "")
        );
        when(
            applicationInternalRestClient.isApplicationExternalIdentifierEnabled(any(), eq("IMPORT_UNIT_SCHEMA"))
        ).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        RequestResponse validResponse = new RequestResponse() {
            @Override
            public Response toResponse() {
                return null;
            }
        };
        validResponse.setHttpCode(HttpStatus.OK.value());
        when(importSchemaService.importUnitSchema(any(VitamContext.class), any(List.class))).thenReturn(validResponse);

        // When Then
        assertThatCode(
            () -> schemaInternalService.importUnitSchema(vitamContext, multipartFile)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Import should throw BadRequestException when sending to Vitam")
    public void importShouldThrowBadRequestExceptionWhenSendingToVitam()
        throws IOException, AccessExternalClientException, InvalidParseOperationException {
        // Given
        VitamContext vitamContext = new VitamContext(0);
        String fileName = "import_schema_invalid.csv";
        MultipartFile multipartFile = new MockMultipartFile(
            fileName,
            fileName,
            "text/csv",
            getClass().getResourceAsStream("/data/" + fileName)
        );

        when(internalSecurityService.getHttpContext()).thenReturn(
            new InternalHttpContext(0, "", "", "", "", "", "", "")
        );
        when(
            applicationInternalRestClient.isApplicationExternalIdentifierEnabled(any(), eq("IMPORT_UNIT_SCHEMA"))
        ).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        // Mock the service to throw BadRequestException with "Errors in rows found"
        doThrow(new BadRequestException("Errors in rows found"))
            .when(importSchemaService)
            .importUnitSchema(any(VitamContext.class), any(List.class));

        BadRequestException badRequestException = null;

        // When
        try {
            schemaInternalService.importUnitSchema(vitamContext, multipartFile);
        } catch (BadRequestException e) {
            badRequestException = e;
        }

        // Then
        assertThat(badRequestException).isNotNull();
        assertThat(badRequestException.getMessage()).isEqualTo("Errors in rows found");
    }

    @Test
    @DisplayName("Import should throw InternalServerException when application returns null")
    public void importShouldThrowInternalServerExceptionWhenApplicationReturnsNull() throws IOException {
        // Given
        VitamContext vitamContext = new VitamContext(0);
        String fileName = "import_schema_valid.csv";
        MultipartFile multipartFile = new MockMultipartFile(
            fileName,
            fileName,
            "text/csv",
            getClass().getResourceAsStream("/data/" + fileName)
        );

        when(internalSecurityService.getHttpContext()).thenReturn(
            new InternalHttpContext(0, "", "", "", "", "", "", "")
        );
        when(
            applicationInternalRestClient.isApplicationExternalIdentifierEnabled(any(), eq("IMPORT_UNIT_SCHEMA"))
        ).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        InternalServerException internalServerException = null;

        // When
        try {
            schemaInternalService.importUnitSchema(vitamContext, multipartFile);
        } catch (InternalServerException e) {
            internalServerException = e;
        }

        // Then
        assertThat(internalServerException).isNotNull();
        assertThat(internalServerException.getMessage()).isEqualTo("The result of the API call should not be null");
    }
}
