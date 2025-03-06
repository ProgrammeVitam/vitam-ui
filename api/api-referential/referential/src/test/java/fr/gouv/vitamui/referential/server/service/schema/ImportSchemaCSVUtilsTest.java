package fr.gouv.vitamui.referential.server.service.schema;

import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for ImportSchemaCSVUtils")
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportSchemaCSVUtilsTest {

    @Mock
    private MultipartFile mockFile;

    private static final String TEST_FILE_NAME = "import_schema_valid.csv";
    private static final String TEST_FILE_CONTENT_TYPE = "text/csv";

    // Method to load test file content
    private byte[] loadTestFileContent(String fileName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/" + fileName)) {
            if (inputStream == null) {
                throw new IOException("File not found in resources: " + fileName);
            }
            return inputStream.readAllBytes();
        }
    }

    @Test
    @DisplayName("Should process valid import schema CSV file")
    void shouldProcessValidImportSchemaCSVFile() {
        assertDoesNotThrow(() -> {
            // Load the test file
            byte[] fileContent = loadTestFileContent(TEST_FILE_NAME);

            // Create a mock MultipartFile
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                TEST_FILE_NAME,
                TEST_FILE_CONTENT_TYPE,
                fileContent
            );

            // Call the method under test
            ImportSchemaCSVUtils.checkImportFile(mockMultipartFile);
        });
    }

    @Test
    @DisplayName("Should throw exception for empty or invalid schema file")
    void shouldThrowExceptionForInvalidFile() throws Exception {
        // Arrange: Mock the file to simulate an empty file
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Act & Assert: Verify exception is thrown with the expected message
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> ImportSchemaCSVUtils.checkImportFile(mockFile)
        );

        // Verify exception message
        assertEquals("The file is empty", exception.getMessage());
    }

    @Test
    @DisplayName("Test that constants have the expected values")
    void testConstants() {
        assertEquals("Path", ImportSchemaCSVUtils.PATH);
        assertEquals("Cardinality", ImportSchemaCSVUtils.CARDINALITY);
        assertEquals("IsObject", ImportSchemaCSVUtils.ISOBJECT);
        assertEquals("ShortName", ImportSchemaCSVUtils.SHORTNAME);
        assertEquals("Description", ImportSchemaCSVUtils.DESCRIPTION);
    }

    @Test
    @DisplayName("Should build expected import schema columns with reflection")
    void shouldBuildExpectedImportSchemaColumnsWithReflection() throws Exception {
        // Act: Call the static method to get the columns
        List<?> columns = ImportSchemaCSVUtils.buildImportSchemaColumns();

        // Assert: Verify the columns' properties
        assertNotNull(columns, "Columns should not be null");
        assertEquals(5, columns.size(), "There should be 5 columns");

        // Use reflection to inspect ColumnDetails fields
        Object column0 = columns.get(0);

        Field columnNameField = column0.getClass().getDeclaredField("columnName");
        columnNameField.setAccessible(true);
        assertEquals("Path", columnNameField.get(column0));

        Field columnTypeField = column0.getClass().getDeclaredField("columnType");
        columnTypeField.setAccessible(true);
        assertEquals("STRING", columnTypeField.get(column0).toString()); // Compare string values

        Field mandatoryField = column0.getClass().getDeclaredField("mandatory");
        mandatoryField.setAccessible(true);
        assertTrue((boolean) mandatoryField.get(column0));
    }
}
