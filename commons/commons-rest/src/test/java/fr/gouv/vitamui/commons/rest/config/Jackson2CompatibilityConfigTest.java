package fr.gouv.vitamui.commons.rest.config;

import fr.gouv.vitamui.commons.rest.RestTestApplication;
import fr.gouv.vitamui.commons.rest.controller.TestController;
import fr.gouv.vitamui.commons.rest.converter.Jackson2JsonNodeHttpMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link Jackson2CompatibilityConfig}.
 *
 * <p>Verifies, in a real Spring Boot 4 context, that importing {@code Jackson2CompatibilityConfig}:
 * <ol>
 *   <li>Allows a controller with {@code @RequestBody com.fasterxml.jackson.databind.JsonNode} to
 *       deserialize a JSON body without error (the default Jackson 3 converter alone cannot do
 *       this — see {@link Jackson2CompatibilityConfigRegressionTest}).</li>
 *   <li>Places {@link Jackson2JsonNodeHttpMessageConverter} ahead of the default Jackson 3
 *       converter in the resolved {@code HttpMessageConverter} list.</li>
 * </ol>
 *
 * <p>{@link Jackson2CompatibilityConfig} lives in a sub-package of {@link RestTestApplication}'s
 * base package and is therefore auto-scanned in this test context — no explicit {@code @Import} is
 * needed here (unlike in production microservices where it must be wired with {@code @Import}).
 */
@SpringBootTest(classes = RestTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class Jackson2CompatibilityConfigTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Test
    void whenJackson2CompatibilityConfigActive_thenJsonNodeDeserializationSucceeds() {
        restTestClient
            .post()
            .uri(TestController.JACKSON2_JSON_NODE)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"key\": \"value\"}")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.OK)
            .expectBody(String.class)
            .isEqualTo("value");
    }

    @Test
    void whenJackson2CompatibilityConfigActive_thenNestedJsonNodeDeserializationSucceeds() {
        restTestClient
            .post()
            .uri(TestController.JACKSON2_JSON_NODE_PROPERTY)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"jsonNode\": {\"key\": \"value\"}}")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.OK)
            .expectBody(String.class)
            .isEqualTo("value");
    }

    @Test
    void whenJackson2CompatibilityConfigActive_thenJackson2ConverterIsFirstInList() {
        final List<HttpMessageConverter<?>> converters = handlerAdapter.getMessageConverters();
        assertThat(converters).isNotEmpty().first().isInstanceOf(Jackson2JsonNodeHttpMessageConverter.class);
    }

    @Test
    void whenJackson2CompatibilityConfigActive_thenJsonNodeSerializationSucceeds() {
        restTestClient
            .get()
            .uri(TestController.JACKSON2_JSON_NODE_RETURN)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.OK)
            .expectBody()
            .jsonPath("$.key")
            .isEqualTo("value")
            // Garde-fou explicite contre une régression vers le bug des flags
            .jsonPath("$.nodeType")
            .doesNotExist()
            .jsonPath("$.object")
            .doesNotExist();
    }
}
