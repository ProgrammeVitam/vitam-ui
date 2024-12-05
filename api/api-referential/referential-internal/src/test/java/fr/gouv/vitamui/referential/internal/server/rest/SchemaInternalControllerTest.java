package fr.gouv.vitamui.referential.internal.server.rest;

import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.exception.InvalidFileSanitizeException;
import fr.gouv.vitamui.referential.common.dto.SchemaDto;
import fr.gouv.vitamui.referential.common.exception.NoCollectionException;
import fr.gouv.vitamui.referential.common.model.Collection;
import fr.gouv.vitamui.referential.internal.server.schema.SchemaInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchemaInternalControllerTest {

    @InjectMocks
    private SchemaInternalController schemaInternalController;

    @Mock
    private SchemaInternalService schemaInternalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSchemas_withEmptyCollections_throwsNoCollectionException() {
        // Arrange
        Set<Collection> collections = Collections.emptySet();

        // Act & Assert
        assertThrows(NoCollectionException.class, () -> schemaInternalController.getSchemas(collections));
        verify(schemaInternalService, never()).getSchemas(any());
    }

    @Test
    void testImportSchema_withInvalidFileName_throwsException() {
        // Arrange
        String fileName = "invalid/file";
        MultipartFile file = mock(MultipartFile.class);

        // Act & Assert
        assertThrows(
            InvalidFileSanitizeException.class,
            () -> schemaInternalController.importUnitSchema(fileName, file)
        );

        // Verify no further processing occurs
        verify(schemaInternalService, never()).importUnitSchema(any(), any());
    }

    @Test
    void testGetSchemas_NoCollectionException() {
        Set<Collection> collections = Set.of(); // Empty set

        assertThrows(NoCollectionException.class, () -> schemaInternalController.getSchemas(collections));
    }

    @Test
    void testGetArchiveUnitProfileSchema_Success() throws VitamClientException {
        String id = "123";
        SchemaDto schemaDto = new SchemaDto();
        when(schemaInternalService.getArchiveUnitProfileSchema(id)).thenReturn(schemaDto);

        ResponseEntity<SchemaDto> response = schemaInternalController.getArchiveUnitProfileSchema(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
