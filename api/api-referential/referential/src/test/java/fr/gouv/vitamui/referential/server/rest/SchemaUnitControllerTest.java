package fr.gouv.vitamui.referential.server.rest;

import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.server.service.schema.SchemaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchemaUnitControllerTest {

    @InjectMocks
    private SchemaUnitController schemaUnitController;

    @Mock
    private SchemaService schemaService;

    @Mock
    private SecurityService securityService;

    private static final String TEST_FILE_NAME = "import_schema_valid.csv";
    private static final String TEST_FILE_NAME_INVALID = "import_schema_invalid.csv";
    private static final String TEST_FILE_CONTENT_TYPE = "text/csv";

    // method to load file from resources
    private byte[] loadTestFileContent(String fileName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/" + fileName)) {
            if (inputStream == null) {
                throw new IOException("File not found in resources: " + fileName);
            }
            return inputStream.readAllBytes();
        }
    }

    @Test
    @DisplayName("Test importUnitSchema with valid file and successful response")
    void testImportSchemas_Success() throws IOException {
        // Given
        ResponseEntity<Void> expectedResponse = mock(ResponseEntity.class);

        // Mock schemaExternalService.importUnitSchemas behavior
        when(schemaService.importUnitSchema(any())).thenReturn(expectedResponse);

        // Prepare a valid file using the file in the resources
        byte[] fileContent = loadTestFileContent(TEST_FILE_NAME);
        MockMultipartFile validFile = new MockMultipartFile(
            "file",
            TEST_FILE_NAME,
            TEST_FILE_CONTENT_TYPE,
            fileContent
        );

        // When
        ResponseEntity<Void> actualResponse = schemaUnitController.importUnitSchemas(validFile);

        // Then
        assertNotNull(actualResponse, "The response should not be null.");
        assertEquals(expectedResponse, actualResponse, "The actual response should match the expected response.");
    }

    @Test
    @DisplayName("Test importUnitSchema with null file")
    void testImportSchemas_NullFile() {
        // When & Then
        assertThrows(
            IllegalArgumentException.class,
            () -> schemaUnitController.importUnitSchemas(null),
            "Importing schemas with a null file should throw IllegalArgumentException."
        );
    }

    @Test
    @DisplayName("Test importUnitSchema with invalid file content")
    void testImportSchemas_InvalidFileContent() throws IOException {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            TEST_FILE_NAME_INVALID,
            TEST_FILE_CONTENT_TYPE,
            new byte[0]
        );

        // When & Then
        assertThrows(
            IllegalArgumentException.class,
            () -> schemaUnitController.importUnitSchemas(invalidFile),
            "Importing schemas with an empty file should throw IllegalArgumentException."
        );
    }

    @Test
    @DisplayName("Test importUnitSchema when external service throws an exception")
    void testImportSchemas_ExternalServiceException() throws IOException {
        // Given
        MockMultipartFile validFile = new MockMultipartFile(
            "file",
            TEST_FILE_NAME,
            TEST_FILE_CONTENT_TYPE,
            "valid content".getBytes()
        );

        when(schemaService.importUnitSchema(any())).thenThrow(
            new RuntimeException("Mocked exception from schemaExternalService")
        );

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> schemaUnitController.importUnitSchemas(validFile),
            "An exception should be thrown when the external service fails."
        );

        assertEquals(
            "Mocked exception from schemaExternalService",
            exception.getMessage(),
            "The exception message should match the mocked message."
        );
    }
}
