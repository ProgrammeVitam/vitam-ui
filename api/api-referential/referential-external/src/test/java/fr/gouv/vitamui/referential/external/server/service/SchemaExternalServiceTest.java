package fr.gouv.vitamui.referential.external.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.referential.internal.client.SchemaInternalWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaExternalServiceTest {

    private static final String FILENAME = "import_schema.csv";

    @Mock
    private SchemaInternalWebClient schemaInternalWebClient;

    @InjectMocks
    private SchemaExternalService schemaExternalService;

    @Mock
    private InternalHttpContext internalHttpContext;

    private MultipartFile file;

    @BeforeEach
    void setUp() {
        file = new MockMultipartFile("file", FILENAME, "text/csv", "sample data".getBytes());
    }

    @Test
    @DisplayName("Test importUnitSchemas method - Successful import")
    void testImportUnitSchemas_Success() {
        // Arrange
        JsonNode mockResponse = mock(JsonNode.class);
        when(schemaInternalWebClient.importUnitSchema(any(), any(), any())).thenReturn(mockResponse);

        // Act
        JsonNode result = schemaExternalService.importUnitSchemas(internalHttpContext, FILENAME, file);

        // Assert
        verify(schemaInternalWebClient, times(1)).importUnitSchema(internalHttpContext, FILENAME, file);
        assertNotNull(result);
        assertEquals(mockResponse, result);
    }

    @Test
    @DisplayName("Test importUnitSchemas method - File upload failure")
    void testImportUnitSchemas_Failure() {
        // Arrange
        when(schemaInternalWebClient.importUnitSchema(any(), any(), any())).thenThrow(
            new RuntimeException("Failed to import schema")
        );

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> schemaExternalService.importUnitSchemas(internalHttpContext, FILENAME, file)
        );
        assertEquals("Failed to import schema", exception.getMessage());
    }

    @Test
    @DisplayName("Test importUnitSchemas method - Null filename")
    void testImportUnitSchemas_NullFilename() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> schemaExternalService.importUnitSchemas(internalHttpContext, null, file)
        );
        assertEquals("Filename cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Test importUnitSchemas method - Null file")
    void testImportUnitSchemas_NullFile() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> schemaExternalService.importUnitSchemas(internalHttpContext, FILENAME, null)
        );
        assertEquals("File cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Test importUnitSchemas method - Empty file")
    void testImportUnitSchemas_EmptyFile() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile("file", FILENAME, "text/csv", new byte[0]);
        when(schemaInternalWebClient.importUnitSchema(any(), any(), any())).thenReturn(null);

        // Act
        JsonNode result = schemaExternalService.importUnitSchemas(internalHttpContext, FILENAME, emptyFile);

        // Assert
        verify(schemaInternalWebClient, times(1)).importUnitSchema(internalHttpContext, FILENAME, emptyFile);
        assertNull(result);
    }
}
