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
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RestTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class Jackson2CompatibilityConfigTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Test
    void jackson2ConverterComesBeforeJackson3() {
        final List<HttpMessageConverter<?>> converters = handlerAdapter.getMessageConverters();

        final int jackson2 = indexOf(converters, Jackson2GenericHttpMessageConverter.class);
        final int jackson3 = indexOf(converters, JacksonJsonHttpMessageConverter.class);

        assertThat(jackson2).as("Jackson 2 converter registered").isNotNegative();
        assertThat(jackson3).as("Jackson 3 converter registered").isNotNegative();
        assertThat(jackson2).as("Jackson 2 converter before Jackson 3").isLessThan(jackson3);
    }

    @Test
    void nestedJsonNodeIsDeserialized() {
        restTestClient
            .post()
            .uri(TestController.JACKSON2_NESTED_JSON_NODE)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"nested\":{\"nested\":{\"payload\":{\"key\":\"value\"}}}}")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.OK)
            .expectBody(String.class)
            .isEqualTo("value");
    }

    private static int indexOf(final List<HttpMessageConverter<?>> converters, final Class<?> type) {
        for (int i = 0; i < converters.size(); i++) {
            if (type.isInstance(converters.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
