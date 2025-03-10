package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.administration.schema.SchemaCardinality;
import fr.gouv.vitam.common.model.administration.schema.SchemaInputModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportSchemaCommonServiceTest {

    @Mock
    private Logger logger;

    private VitamContext vitamContext;
    private AdminExternalClient adminExternalClient;
    private ImportSchemaCommonService importSchemaCommonService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        vitamContext = mock(VitamContext.class);
        adminExternalClient = mock(AdminExternalClient.class);
        importSchemaCommonService = new ImportSchemaCommonService(adminExternalClient);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should throw AccessExternalClientException when import schema encounters an error")
    void importSchema_ShouldThrowAccessExternalClientException_OnError() throws Exception {
        // Arrange
        when(vitamContext.getApplicationSessionId()).thenReturn("testSessionId");

        SchemaInputModel schemaModel = new SchemaInputModel();
        List<SchemaInputModel> schemaModels = Collections.singletonList(schemaModel);

        when(adminExternalClient.importUnitExternalSchema(any(), any())).thenThrow(AccessExternalClientException.class);

        // Act & Assert
        assertThrows(
            AccessExternalClientException.class,
            () -> importSchemaCommonService.importUnitSchema(vitamContext, schemaModels)
        );
    }

    @Test
    @DisplayName("Should not throw an exception during schema serialization")
    void serializeImportSchema_ShouldNotThrowException() {
        // Arrange
        SchemaInputModel schemaModel = new SchemaInputModel();
        schemaModel.setPath("testPath");
        schemaModel.setCardinality(SchemaCardinality.ONE);
        schemaModel.setObject(true);
        schemaModel.setShortName("testShortName");
        schemaModel.setDescription("testDescription");

        List<SchemaInputModel> schemaModels = Collections.singletonList(schemaModel);

        // Act & Assert
        assertDoesNotThrow(() -> importSchemaCommonService.serializeImportSchema(schemaModels));
    }

    @Test
    @DisplayName("Should throw AccessExternalClientException for invalid schema")
    void importSchema_ShouldThrowExceptionForInvalidSchema() throws Exception {
        // Arrange
        SchemaInputModel invalidSchemaModel = new SchemaInputModel(); // Missing required fields
        List<SchemaInputModel> invalidSchemaList = Collections.singletonList(invalidSchemaModel);

        when(adminExternalClient.importUnitExternalSchema(any(), any())).thenThrow(AccessExternalClientException.class);

        // Act & Assert
        assertThrows(
            AccessExternalClientException.class,
            () -> importSchemaCommonService.importUnitSchema(vitamContext, invalidSchemaList)
        );
    }

    @Test
    @DisplayName("Should handle null schema list during serialization")
    void serializeImportSchema_ShouldHandleNullSchemaList() throws IOException {
        // Arrange
        List<SchemaInputModel> nullSchemaList = null;

        // Act
        ByteArrayInputStream result = importSchemaCommonService.serializeImportSchema(nullSchemaList);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle empty schema list during serialization")
    void serializeImportSchema_ShouldHandleEmptySchemaList() throws IOException {
        // Arrange
        List<SchemaInputModel> emptySchemaList = Collections.emptyList();

        // Act
        ByteArrayInputStream result = importSchemaCommonService.serializeImportSchema(emptySchemaList);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when import schema response is null")
    void importSchema_ShouldThrowException_WhenResponseIsNull()
        throws AccessExternalClientException, InvalidParseOperationException {
        // Arrange
        SchemaInputModel schemaModel = new SchemaInputModel();
        schemaModel.setPath("testPath");
        List<SchemaInputModel> schemaModels = Collections.singletonList(schemaModel);

        when(adminExternalClient.importUnitExternalSchema(any(), any())).thenReturn(null);

        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> importSchemaCommonService.importUnitSchema(vitamContext, schemaModels)
        );
    }

    @Test
    @DisplayName("Should generate valid JSON when serializing SchemaInputModel list")
    void testSerializeImportSchema_shouldGenerateValidJson() throws IOException {
        // Arrange: Create a list of SchemaInputModel with sample data
        SchemaInputModel schema1 = new SchemaInputModel();
        schema1.setPath("path1");
        schema1.setDescription("description1");
        schema1.setCardinality(SchemaCardinality.ONE); // Enum should be serialized as "ONE"
        schema1.setObject(true);
        schema1.setShortName("shortName1");

        SchemaInputModel schema2 = new SchemaInputModel();
        schema2.setPath("path2");
        schema2.setDescription("description2");
        schema2.setCardinality(SchemaCardinality.ONE); // Enum should be serialized as "ONE"
        schema2.setObject(true);
        schema2.setShortName("shortName2");

        List<SchemaInputModel> schemaList = Arrays.asList(schema1, schema2);

        // Act: Call the method to serialize the schema
        ByteArrayInputStream result = importSchemaCommonService.serializeImportSchema(schemaList);

        // Assert: Convert the result back to a string and validate its contents
        String jsonString = new String(result.readAllBytes());
        System.out.println(jsonString); // Optional: For debugging

        // Assert using AssertJ to validate all fields of the JSON
        assertThat(jsonString).isNotNull();

        // First schema
        assertThat(jsonString).contains("\"Path\":\"path1\"");
        assertThat(jsonString).contains("\"Description\":\"description1\"");
        assertThat(jsonString).contains("\"Cardinality\":\"ONE\""); // Enum value
        assertThat(jsonString).contains("\"IsObject\":true");
        assertThat(jsonString).contains("\"ShortName\":\"shortName1\"");

        // Second schema
        assertThat(jsonString).contains("\"Path\":\"path2\"");
        assertThat(jsonString).contains("\"Description\":\"description2\"");
        assertThat(jsonString).contains("\"Cardinality\":\"ONE\""); // Enum value
        assertThat(jsonString).contains("\"IsObject\":true");
        assertThat(jsonString).contains("\"ShortName\":\"shortName2\"");

        // Additional: Convert back to object and ensure it matches the original
        List<?> deserializedList = objectMapper.readValue(jsonString, List.class);
        assertThat(deserializedList).hasSize(2);
    }
}
