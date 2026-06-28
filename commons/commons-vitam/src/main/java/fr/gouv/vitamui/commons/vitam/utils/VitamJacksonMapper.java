package fr.gouv.vitamui.commons.vitam.utils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class VitamJacksonMapper {

    private static final ObjectMapper jackson3Mapper = new ObjectMapper();
    private static final com.fasterxml.jackson.databind.ObjectMapper jackson2Mapper =
        new com.fasterxml.jackson.databind.ObjectMapper();

    public static JsonNode mapToJackson3(com.fasterxml.jackson.databind.JsonNode jackson2Node) {
        if (jackson2Node == null) return null;
        try {
            return jackson3Mapper.readTree(jackson2Mapper.writeValueAsBytes(jackson2Node));
        } catch (Exception e) {
            throw new RuntimeException("Error mapping to Jackson 3", e);
        }
    }

    public static com.fasterxml.jackson.databind.JsonNode mapToJackson2(JsonNode jackson3Node) {
        if (jackson3Node == null) return null;
        try {
            return jackson2Mapper.readTree(jackson3Mapper.writeValueAsBytes(jackson3Node));
        } catch (Exception e) {
            throw new RuntimeException("Error mapping to Jackson 2", e);
        }
    }
}
