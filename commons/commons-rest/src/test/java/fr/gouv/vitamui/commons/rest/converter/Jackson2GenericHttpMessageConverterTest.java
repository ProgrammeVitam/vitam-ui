package fr.gouv.vitamui.commons.rest.converter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Behavioural tests for {@link Jackson2GenericHttpMessageConverter}: proves that a Jackson 2 converter
 * correctly (de)serializes the types the default Jackson 3 converter cannot handle, without depending on
 * the deprecated Spring class {@code MappingJackson2HttpMessageConverter}.
 *
 * <p>Standalone {@link MockMvc} setup registering ONLY this converter (what the config inserts before
 * Jackson 3). Covers the cases the old reflection-based approach missed:
 * <ol>
 *   <li>bare {@code @RequestBody JsonNode};</li>
 *   <li>{@code @RequestBody} of a DTO containing a {@code JsonNode} (bug #16637, like
 *       {@code ProbativeValueRequest});</li>
 *   <li>{@code @RequestBody List<JsonNode>} — a generic type resolved through the {@code JavaType};</li>
 *   <li>{@code @ResponseBody} of a DTO containing a {@code JsonNode} — the write side.</li>
 * </ol>
 */
class Jackson2GenericHttpMessageConverterTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BehaviourController())
            .setMessageConverters(new Jackson2GenericHttpMessageConverter(new ObjectMapper()))
            .build();
    }

    /** Application DTO containing a Jackson 2 JsonNode, like {@code ProbativeValueRequest}. */
    record ReportRequest(JsonNode dslQuery, String usage) {
        @JsonCreator
        ReportRequest(@JsonProperty("dslQuery") final JsonNode dslQuery, @JsonProperty("usage") final String usage) {
            this.dslQuery = dslQuery;
            this.usage = usage;
        }
    }

    @RestController
    static class BehaviourController {

        @PostMapping(value = "/behaviour/jsonNode", consumes = MediaType.APPLICATION_JSON_VALUE)
        public @ResponseBody String jsonNode(@RequestBody final JsonNode body) {
            return body.path("key").asText();
        }

        @PostMapping(value = "/behaviour/dto", consumes = MediaType.APPLICATION_JSON_VALUE)
        public @ResponseBody String dto(@RequestBody final ReportRequest body) {
            return body.dslQuery().path("key").asText();
        }

        @PostMapping(value = "/behaviour/listJsonNode", consumes = MediaType.APPLICATION_JSON_VALUE)
        public @ResponseBody String listJsonNode(@RequestBody final List<JsonNode> body) {
            return body.get(0).path("key").asText();
        }

        @PostMapping(
            value = "/behaviour/dtoReturn",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
        )
        public @ResponseBody ReportRequest dtoReturn(@RequestBody final ReportRequest body) {
            return body;
        }
    }

    @Test
    void bareJsonNode_isDeserialized() throws Exception {
        mockMvc
            .perform(post("/behaviour/jsonNode").contentType(MediaType.APPLICATION_JSON).content("{\"key\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("\"value\""));
    }

    @Test
    void dtoWithJsonNode_isDeserialized() throws Exception {
        mockMvc
            .perform(
                post("/behaviour/dto")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dslQuery\":{\"key\":\"value\"},\"usage\":\"BinaryMaster\"}")
            )
            .andExpect(status().isOk())
            .andExpect(content().string("\"value\""));
    }

    @Test
    void listOfJsonNode_isDeserialized() throws Exception {
        // Generic path: without JavaType resolution the list would be deserialized as LinkedHashMap.
        mockMvc
            .perform(
                post("/behaviour/listJsonNode").contentType(MediaType.APPLICATION_JSON).content("[{\"key\":\"value\"}]")
            )
            .andExpect(status().isOk())
            .andExpect(content().string("\"value\""));
    }

    @Test
    void dtoWithJsonNode_isSerializedInResponse() throws Exception {
        mockMvc
            .perform(
                post("/behaviour/dtoReturn")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dslQuery\":{\"key\":\"value\"},\"usage\":\"BinaryMaster\"}")
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"dslQuery\":{\"key\":\"value\"},\"usage\":\"BinaryMaster\"}"));
    }
}
