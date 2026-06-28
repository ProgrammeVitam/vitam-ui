package fr.gouv.vitamui.referential.common.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class ImportSchemaDtoTest {

    private ImportSchemaDto importSchemaDto;

    @BeforeEach
    void setUp() {
        importSchemaDto = new ImportSchemaDto();
    }

    @Test
    void testGettersAndSetters() {
        // Set values
        importSchemaDto.setPath("test/path");
        importSchemaDto.setCardinality("1..n");
        importSchemaDto.setObject(true);
        importSchemaDto.setShortName("Test ShortName");
        importSchemaDto.setDescription("Test Description");

        // Verify getters return the expected values
        assertEquals("test/path", importSchemaDto.getPath());
        assertEquals("1..n", importSchemaDto.getCardinality());
        assertTrue(importSchemaDto.isObject());
        assertEquals("Test ShortName", importSchemaDto.getShortName());
        assertEquals("Test Description", importSchemaDto.getDescription());
    }

    @Test
    void testSerialization() throws Exception {
        // Set values
        importSchemaDto.setPath("test/path");
        importSchemaDto.setCardinality("1..1");
        importSchemaDto.setObject(false);
        importSchemaDto.setShortName("UniqueName");
        importSchemaDto.setDescription("A short description");

        // Serialize to JSON (using Jackson)
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(importSchemaDto);

        // Assert the JSON string contains the expected values
        assertTrue(json.contains("test/path"));
        assertTrue(json.contains("1..1"));
        assertTrue(json.contains("UniqueName"));
        assertTrue(json.contains("A short description"));
    }

    @Test
    void testToString() {
        // Set values
        importSchemaDto.setPath("test/path");
        importSchemaDto.setCardinality("1..1");
        importSchemaDto.setObject(true);
        importSchemaDto.setShortName("TestShort");
        importSchemaDto.setDescription("A description");

        // Verify the toString method contains expected values
        String toString = importSchemaDto.toString();
        assertTrue(toString.contains("test/path"));
        assertTrue(toString.contains("1..1"));
        assertTrue(toString.contains("TestShort"));
        assertTrue(toString.contains("A description"));
    }
}
