package fr.gouv.vitamui.commons.api.exception;

import fr.gouv.vitamui.commons.api.ApplicationTest;
import fr.gouv.vitamui.commons.api.controller.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(classes = ApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureRestTestClient
class ExceptionTest {

    @Autowired
    private RestTestClient restClient;

    @Test
    void testApplicationServerException() {
        restClient
            .get()
            .uri(TestController.APPLICATION_SERVER_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testBadRequestException() {
        restClient.get().uri(TestController.BAD_REQUEST_EXCEPTION).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testForbiddenException() {
        restClient.get().uri(TestController.FORBIDDEN_EXCEPTION).exchange().expectStatus().isForbidden();
    }

    @Test
    void testNotFoundException() {
        restClient.get().uri(TestController.NOT_FOUND_EXCEPTION).exchange().expectStatus().isNotFound();
    }

    @Test
    void testUnAuthorizedException() {
        restClient.get().uri(TestController.UN_AUTHORIZED_EXCEPTION).exchange().expectStatus().isUnauthorized();
    }

    @Test
    void testNotImplementedException() {
        restClient
            .get()
            .uri(TestController.NOT_IMPLEMENTED_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    void testParseOperationException() {
        restClient.get().uri(TestController.PARSE_OPERATION_EXCEPTION).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testParseOperationExceptionWithThrowable() {
        restClient
            .get()
            .uri(TestController.PARSE_OPERATION_EXCEPTION_WITH_THROWABLE)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    void testRouteNotFoundException() {
        restClient.get().uri(TestController.ROUTE_NOT_FOUND_EXCEPTION).exchange().expectStatus().isNotFound();
    }

    @Test
    void testValidationException() {
        restClient.get().uri(TestController.VALIDATION_EXCEPTION).exchange().expectStatus().isBadRequest();
    }
}
