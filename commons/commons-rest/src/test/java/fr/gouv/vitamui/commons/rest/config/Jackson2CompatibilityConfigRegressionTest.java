package fr.gouv.vitamui.commons.rest.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Regression test documenting the failure that {@link Jackson2CompatibilityConfig} was introduced to fix.
 *
 * <p>When only the default Spring Boot 4 Jackson 3 converter
 * ({@link JacksonJsonHttpMessageConverter}, backed by {@code tools.jackson.databind.JsonMapper})
 * is active, it cannot instantiate the abstract Jackson 2 type
 * {@code com.fasterxml.jackson.databind.JsonNode}. Spring MVC raises an
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
    }

    @Test
    void whenOnlyJackson3ConverterPresent_thenJsonNodeDeserializationFails() {
        // Without Jackson2JsonNodeHttpMessageConverter, the Jackson 3 converter (JacksonJsonHttpMessageConverter)
        // raises HttpMessageConversionException because it cannot construct the abstract Jackson 2 type
        // com.fasterxml.jackson.databind.JsonNode. The exception propagates out of MockMvc since
        // DefaultHandlerExceptionResolver does not handle HttpMessageConversionException.
        assertThatThrownBy(
            () ->
                mockMvc.perform(
                    post("/test/jsonNode").contentType(MediaType.APPLICATION_JSON).content("{\"key\": \"value\"}")
                )
        )
            .hasMessageContaining("Type definition error")
            .hasMessageContaining("com.fasterxml.jackson.databind.JsonNode");
    }
}
