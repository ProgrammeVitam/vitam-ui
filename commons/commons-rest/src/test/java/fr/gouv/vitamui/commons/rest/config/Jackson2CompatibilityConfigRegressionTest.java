package fr.gouv.vitamui.commons.rest.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression test documenting the failure that {@link Jackson2CompatibilityConfig} was introduced to fix.
 *
 * <p>When only the default Spring Boot 4 Jackson 3 converter
 * ({@link JacksonJsonHttpMessageConverter}, backed by {@code tools.jackson.databind.JsonMapper})
 * is active, it cannot instantiate the abstract Jackson 2 type
 * {@code tools.jackson.databind.JsonNode}. Spring MVC raises an
 * {@code HttpMessageConversionException} with "Type definition error" on any endpoint
 * declaring {@code @RequestBody JsonNode}.
 *
 * <p>This test uses a standalone {@link MockMvc} setup to control the converter list precisely,
 * bypassing the full Spring Boot context (where {@link Jackson2CompatibilityConfig} is always
 * present via component scan). The counterpart passing test is
 * {@link Jackson2CompatibilityConfigTest}.
 */
class Jackson2CompatibilityConfigRegressionTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Register only the Jackson 3 converter — no Jackson2JsonNodeHttpMessageConverter
        mockMvc = MockMvcBuilders.standaloneSetup(new JsonNodeController())
            .setMessageConverters(new JacksonJsonHttpMessageConverter())
            .build();
    }

    @RestController
    static class JsonNodeController {

        @PostMapping(value = "/test/jsonNode", consumes = MediaType.APPLICATION_JSON_VALUE)
        public String echo(@RequestBody final JsonNode body) {
            return body.path("key").asText();
        }

        @GetMapping(value = "/test/jsonNodeReturn", produces = MediaType.APPLICATION_JSON_VALUE)
        public JsonNode jsonNodeReturn() {
            final ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("key", "value");
            return node;
        }
    }

    @Test
    void whenOnlyJackson3ConverterPresent_thenJsonNodeDeserializationFails() {
        // Without Jackson2JsonNodeHttpMessageConverter, the Jackson 3 converter (JacksonJsonHttpMessageConverter)
        // raises HttpMessageConversionException because it cannot construct the abstract Jackson 2 type
        // tools.jackson.databind.JsonNode. The exception propagates out of MockMvc since
        // DefaultHandlerExceptionResolver does not handle HttpMessageConversionException.
        assertThatThrownBy(
            () ->
                mockMvc.perform(
                    post("/test/jsonNode").contentType(MediaType.APPLICATION_JSON).content("{\"key\": \"value\"}")
                )
        )
            .hasMessageContaining("Type definition error")
            .hasMessageContaining("tools.jackson.databind.JsonNode");
    }

    @Test
    void whenOnlyJackson3ConverterPresent_thenJsonNodeSerializationReturnsMetadataInsteadOfData() throws Exception {
        // Sans Jackson2JsonNodeHttpMessageConverter (canWrite actif), le converter Jackson 3 par défaut
        // traite le JsonNode Jackson 2 (abstrait pour lui) comme un POJO classique et sérialise ses
        // getters isXxx()/nodeType au lieu du contenu JSON réel.
        final MvcResult result = mockMvc.perform(get("/test/jsonNodeReturn")).andExpect(status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();

        assertThat(body).contains("\"nodeType\"").doesNotContain("\"key\":\"value\"");
    }
}
