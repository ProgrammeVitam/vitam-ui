package fr.gouv.vitamui.referential.internal.server.rest;

import fr.gouv.vitamui.commons.api.exception.InvalidFileSanitizeException;
import fr.gouv.vitamui.referential.internal.server.schema.SchemaInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SchemaUnitInternalControllerTest {

    @InjectMocks
    private SchemaUnitInternalController schemaUnitInternalController;

    @Mock
    private SchemaInternalService schemaInternalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testImportSchema_withInvalidFileName_throwsException() {
        // Arrange
        String fileName = "invalid/file";
        MultipartFile file = mock(MultipartFile.class);

        // Act & Assert
        assertThrows(
            InvalidFileSanitizeException.class,
            () -> schemaUnitInternalController.importUnitSchema(fileName, file)
        );

        // Verify no further processing occurs
        verify(schemaInternalService, never()).importUnitSchema(any(), any());
    }
}
