package fr.gouv.vitamui.referential.server.service.schema;

import fr.gouv.vitam.common.model.administration.schema.SchemaCardinality;
import fr.gouv.vitam.common.model.administration.schema.SchemaInputModel;
import fr.gouv.vitamui.referential.common.dto.ImportSchemaDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportSchemaConverterTest {

    private final ImportSchemaConverter converter = new ImportSchemaConverter();

    @Test
    @DisplayName("Convert ImportSchemaDto to SchemaInputModel")
    void testConvertDtoToVitam() {
        ImportSchemaDto dto = new ImportSchemaDto();
        dto.setPath("path");
        dto.setCardinality("ONE");
        dto.setObject(true);
        dto.setShortName("shortName");
        dto.setDescription("description");

        SchemaInputModel schemaInputModel = converter.convertDtoToVitam(dto);

        assertNotNull(schemaInputModel);
        assertEquals("path", schemaInputModel.getPath());
        assertEquals(SchemaCardinality.ONE, schemaInputModel.getCardinality());
        assertTrue(schemaInputModel.isObject());
        assertEquals("shortName", schemaInputModel.getShortName());
        assertEquals("description", schemaInputModel.getDescription());
    }

    @Test
    @DisplayName("Convert SchemaInputModel to ImportSchemaDto")
    void testConvertVitamToDto() {
        SchemaInputModel schemaInputModel = new SchemaInputModel();
        schemaInputModel.setPath("path");
        schemaInputModel.setCardinality(SchemaCardinality.ONE);
        schemaInputModel.setObject(true);
        schemaInputModel.setShortName("shortName");
        schemaInputModel.setDescription("description");

        ImportSchemaDto dto = converter.convertVitamToDto(schemaInputModel);

        assertNotNull(dto);
        assertEquals("path", dto.getPath());
        assertEquals("ONE", dto.getCardinality());
        assertTrue(dto.isObject());
        assertEquals("shortName", dto.getShortName());
        assertEquals("description", dto.getDescription());
    }

    @Test
    @DisplayName("Convert list of ImportSchemaDto to list of SchemaInputModel")
    void testConvertDtosToVitams() {
        ImportSchemaDto dto1 = new ImportSchemaDto();
        dto1.setPath("path1");
        dto1.setCardinality("ONE");

        ImportSchemaDto dto2 = new ImportSchemaDto();
        dto2.setPath("path2");
        dto2.setCardinality("MANY");

        List<ImportSchemaDto> dtos = List.of(dto1, dto2);

        List<SchemaInputModel> result = converter.convertDtosToVitams(dtos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("path1", result.get(0).getPath());
        assertEquals(SchemaCardinality.ONE, result.get(0).getCardinality());
        assertEquals("path2", result.get(1).getPath());
        assertEquals(SchemaCardinality.MANY, result.get(1).getCardinality());
    }

    @Test
    @DisplayName("Convert list of SchemaInputModel to list of ImportSchemaDto")
    void testConvertVitamsToDtos() {
        SchemaInputModel model1 = new SchemaInputModel();
        model1.setPath("path1");
        model1.setCardinality(SchemaCardinality.ONE);

        SchemaInputModel model2 = new SchemaInputModel();
        model2.setPath("path2");
        model2.setCardinality(SchemaCardinality.MANY);

        List<SchemaInputModel> importSchemas = List.of(model1, model2);

        List<ImportSchemaDto> result = converter.convertVitamsToDtos(importSchemas);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("path1", result.get(0).getPath());
        assertEquals("ONE", result.get(0).getCardinality());
        assertEquals("path2", result.get(1).getPath());
        assertEquals("MANY", result.get(1).getCardinality());
        assertFalse(result.get(1).isObject());
    }

    @Test
    @DisplayName("Copy properties from SchemaInputModel to ImportSchemaDto")
    void testCopyCustomProperties_fromModelToDto() {
        SchemaInputModel model = new SchemaInputModel();
        model.setCardinality(SchemaCardinality.ONE);
        model.setPath("path/to/schema");
        model.setObject(false);
        model.setShortName("shortName");
        model.setDescription("Test description");

        ImportSchemaDto dto = new ImportSchemaDto();

        converter.copyCustomProperties(model, dto);

        assertEquals("ONE", dto.getCardinality());
        assertEquals("path/to/schema", dto.getPath());
        assertFalse(dto.isObject());
        assertEquals("shortName", dto.getShortName());
        assertEquals("Test description", dto.getDescription());
    }

    @Test
    @DisplayName("Convert valid ImportSchemaDto to SchemaInputModel")
    void testConvertDtoToVitam_validDto() {
        ImportSchemaDto dto = new ImportSchemaDto();
        dto.setCardinality("MANY");
        dto.setPath("path/to/schema");
        dto.setObject(true);
        dto.setShortName("shortName");
        dto.setDescription("Test description");

        SchemaInputModel result = converter.convertDtoToVitam(dto);

        assertNotNull(result);
        assertEquals("MANY", result.getCardinality().toString());
        assertEquals("path/to/schema", result.getPath());
        assertTrue(result.isObject());
        assertEquals("shortName", result.getShortName());
        assertEquals("Test description", result.getDescription());
    }

    @Test
    @DisplayName("Throw exception when converting null ImportSchemaDto")
    void testConvertDtoToVitam_nullDto() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertDtoToVitam(null);
        });
        assertEquals("DTO cannot be null", thrown.getMessage());
    }

    @Test
    @DisplayName("Throw exception when cardinality is invalid in ImportSchemaDto")
    void testConvertDtoToVitam_invalidCardinality() {
        ImportSchemaDto dto = new ImportSchemaDto();
        dto.setCardinality("INVALID");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertDtoToVitam(dto);
        });
        assertTrue(thrown.getMessage().contains("Invalid cardinality"));
    }

    @Test
    @DisplayName("Convert valid SchemaInputModel to ImportSchemaDto")
    void testConvertVitamToDto_validModel() {
        SchemaInputModel model = new SchemaInputModel();
        model.setCardinality(SchemaCardinality.ONE);
        model.setPath("path/to/schema");
        model.setObject(false);
        model.setShortName("shortName");
        model.setDescription("Test description");

        ImportSchemaDto result = converter.convertVitamToDto(model);

        assertNotNull(result);
        assertEquals("ONE", result.getCardinality());
        assertEquals("path/to/schema", result.getPath());
        assertFalse(result.isObject());
        assertEquals("shortName", result.getShortName());
        assertEquals("Test description", result.getDescription());
    }

    @Test
    @DisplayName("Convert list of ImportSchemaDto to SchemaInputModel and handle empty list")
    void testConvertDtosToVitams_emptyList() {
        List<ImportSchemaDto> dtos = Collections.emptyList();

        List<SchemaInputModel> result = converter.convertDtosToVitams(dtos);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Convert list of SchemaInputModel to ImportSchemaDto and handle empty list")
    void testConvertVitamsToDtos_emptyList() {
        List<SchemaInputModel> models = Collections.emptyList();

        List<ImportSchemaDto> result = converter.convertVitamsToDtos(models);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Validate valid ImportSchemaDto")
    void testValidateDto_validDto() {
        ImportSchemaDto dto = new ImportSchemaDto();
        dto.setCardinality("MANY");

        assertDoesNotThrow(() -> converter.validateDto(dto));
    }

    @Test
    @DisplayName("Throw exception when validating null ImportSchemaDto")
    void testValidateDto_nullDto() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            converter.validateDto(null);
        });
        assertEquals("DTO cannot be null", thrown.getMessage());
    }

    @Test
    @DisplayName("Throw exception when cardinality is invalid in ImportSchemaDto during validation")
    void testValidateDto_invalidCardinality() {
        ImportSchemaDto dto = new ImportSchemaDto();
        dto.setCardinality("INVALID");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            converter.validateDto(dto);
        });
        assertTrue(thrown.getMessage().contains("Invalid cardinality"));
    }

    @Test
    @DisplayName("Convert ImportSchemaDto to SchemaInputModel when isObject is not set")
    void testConvertDtoToVitam_isObjectNotSet() {
        // Arrange: Create ImportSchemaDto without setting isObject
        ImportSchemaDto dto = new ImportSchemaDto();
        dto.setPath("path/to/schema");
        dto.setCardinality("ONE");
        dto.setShortName("shortName");
        dto.setDescription("Test description");

        // Act: Convert to SchemaInputModel
        SchemaInputModel schemaInputModel = converter.convertDtoToVitam(dto);

        // Assert: Validate that isObject is false by default
        assertNotNull(schemaInputModel);
        assertEquals("path/to/schema", schemaInputModel.getPath());
        assertEquals(SchemaCardinality.ONE, schemaInputModel.getCardinality());
        assertFalse(schemaInputModel.isObject(), "The default value of isObject should be false");
        assertEquals("shortName", schemaInputModel.getShortName());
        assertEquals("Test description", schemaInputModel.getDescription());
    }
}
