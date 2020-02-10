/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.utils;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {

    private static final ObjectMapper mapper;

    private static final ObjectMapper mapperDontFailOnUnknowProperties;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());

        mapperDontFailOnUnknowProperties = new ObjectMapper();
        mapperDontFailOnUnknowProperties.registerModule(new JavaTimeModule());
        mapperDontFailOnUnknowProperties.registerModule(new Jdk8Module());
        mapperDontFailOnUnknowProperties.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Convert Object to json string
     * @param Object
     * @return
     * @throws JsonProcessingException
     */
    public static String toJson(final Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    /**
     * Convert value Object to specified Type.
     * @param fromValue
     * @param clazz
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T convertValue(final Object fromValue, final Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return mapper.convertValue(fromValue, clazz);
    }

    /**
     * Convert value Object to specified Type.
     * @param fromValue
     * @param toValueTypeRef
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> List<T> convertValueList(final Object fromValue, final Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        final JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);

        return mapper.convertValue(fromValue, type);
    }

    /**
     * Convert json string to Object
     * @param <T>
     *
     * @param json
     * @param type
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T fromJson(final String json, final TypeReference<T> type) throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(json, type);
    }

    /**
     * Convert json string to Object
     *
     * @param json
     * @param clazz
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T fromJson(final String json, final Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(json, clazz);
    }

    public static JsonNode readTree(final byte[] content) throws IOException {
        return mapper.readTree(content);
    }

    public static JsonNode readTree(final String content) throws IOException {
        return mapper.readTree(content);
    }

    public static <T> T treeToValue(final JsonNode json, final Class<T> clazz) throws JsonProcessingException {
        return mapper.treeToValue(json, clazz);
    }

    public static <T> T treeToValue(final JsonNode json, final Class<T> clazz, final boolean failOnMissingProperties) throws JsonProcessingException {
        ObjectMapper mapperToUse = mapper;
        if (!failOnMissingProperties) {
            mapperToUse = mapperDontFailOnUnknowProperties;
        }
        return mapperToUse.treeToValue(json, clazz);
    }

    /**
     * Convert Object to {@link JsonNode}.
     * @param Object
     * @return
     * @throws JsonProcessingException
     */
    public static JsonNode toJsonNode(final Object object) {
        return mapper.valueToTree(object);
    }

    /**
     *
     * @return An empty ObjectNode
     * @throws IOException
     */
    public static ObjectNode getEmptyObjectNode() throws IOException {
        return (ObjectNode) mapper.readTree("{}");
    }

    /**
     * @return an empty ObjectNode
     */
    public static final ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    /**
     * @return an empty ArrayNode
     */
    public static final ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }
}
