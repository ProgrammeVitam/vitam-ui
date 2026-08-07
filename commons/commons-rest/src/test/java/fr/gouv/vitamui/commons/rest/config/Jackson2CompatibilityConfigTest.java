package fr.gouv.vitamui.commons.rest.config;

import fr.gouv.vitamui.commons.rest.RestTestApplication;
import fr.gouv.vitamui.commons.rest.controller.TestController;
import fr.gouv.vitamui.commons.rest.converter.Jackson2GenericHttpMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
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
 *   <li>Places {@link Jackson2GenericHttpMessageConverter} immediately ahead of the default Jackson 3
 *       converter in the resolved {@code HttpMessageConverter} list — and <b>not</b> at the head, so that
 *       the specialized converters ordered before Jackson 3 keep their priority.</li>
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
    void whenJackson2CompatibilityConfigActive_thenJackson2ConverterSitsJustBeforeJackson3() {
        final List<HttpMessageConverter<?>> converters = handlerAdapter.getMessageConverters();

        final int jackson2 = indexOf(converters, Jackson2GenericHttpMessageConverter.class);
        final int jackson3 = indexOf(converters, JacksonJsonHttpMessageConverter.class);

        assertThat(jackson2).as("Jackson 2 converter registered").isNotNegative();
        assertThat(jackson3).as("Jackson 3 converter still present as fallback").isEqualTo(jackson2 + 1);
    }

    /**
     * The Jackson 2 converter claims every type for {@code application/json} ({@code supports() == true}),
     * so it must stay behind the specialized converters: otherwise a {@code String} return value would go
     * out as a quoted JSON string instead of {@code text/plain}.
     */
    @Test
    void whenJackson2CompatibilityConfigActive_thenSpecializedConvertersKeepPriority() {
        final List<HttpMessageConverter<?>> converters = handlerAdapter.getMessageConverters();

        assertThat(indexOf(converters, StringHttpMessageConverter.class))
            .as("StringHttpMessageConverter still ahead of the Jackson 2 converter")
            .isLessThan(indexOf(converters, Jackson2GenericHttpMessageConverter.class));
    }

    private static int indexOf(final List<HttpMessageConverter<?>> converters, final Class<?> type) {
        for (int i = 0; i < converters.size(); i++) {
            if (type.isInstance(converters.get(i))) {
                return i;
            }
        }
        return -1;
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
