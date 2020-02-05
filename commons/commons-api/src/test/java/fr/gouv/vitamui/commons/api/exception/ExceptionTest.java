package fr.gouv.vitamui.commons.api.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.ApplicationTest;
import fr.gouv.vitamui.commons.api.controller.TestController;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class ExceptionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testApplicationServerException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.APPLICATION_SERVER_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR,
                result.getStatusCode());
    }

    @Test
    public void testApplicationServerExceptionWithThrowable() {
        final ResponseEntity<String> result = restTemplate
                .getForEntity(TestController.APPLICATION_SERVER_EXCEPTION_WITH_THROWABLE, String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR,
                result.getStatusCode());
    }

    @Test
    public void testApplicationServerExceptionWithMessageAndThrowable() {
        final ResponseEntity<String> result = restTemplate
                .getForEntity(TestController.APPLICATION_SERVER_EXCEPTION_WITH_MESSAGE_AND_THROWABLE, String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR,
                result.getStatusCode());
    }

    @Test
    public void testBadRequestException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.BAD_REQUEST_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testBadRequestExceptionWithThrowable() {
        final ResponseEntity<String> result = restTemplate
                .getForEntity(TestController.BAD_REQUEST_EXCEPTION_WITH_THROWABLE, String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testForbiddenException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.FORBIDDEN_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    public void testInternalServerException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.INTERNAL_SERVER_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR,
                result.getStatusCode());
    }

    @Test
    public void testInvalidAuthenticationException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.INVALID_AUTHENTICATION_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    public void testInvalidFormatException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.INVALID_FORMAT_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testNotFoundException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.NOT_FOUND_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testNoRightsException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.NO_RIGHTS_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    public void testNotImplementedException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.NOT_IMPLEMENTED_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.NOT_IMPLEMENTED, result.getStatusCode());
    }

    @Test
    public void testParseOperationException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.PARSE_OPERATION_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testParseOperationExceptionWithThrowable() {
        final ResponseEntity<String> result = restTemplate
                .getForEntity(TestController.PARSE_OPERATION_EXCEPTION_WITH_THROWABLE, String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testRouteNotFoundException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.ROUTE_NOT_FOUND_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testUnAuthorizedException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.UN_AUTHORIZED_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    public void testValidationException() {
        final ResponseEntity<String> result = restTemplate.getForEntity(TestController.VALIDATION_EXCEPTION,
                String.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

}
