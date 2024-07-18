package fr.gouv.vitamui.commons.rest.controller;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NoRightsException;
import fr.gouv.vitamui.commons.api.exception.ParseOperationException;
import fr.gouv.vitamui.commons.api.exception.RequestTimeOutException;
import fr.gouv.vitamui.commons.api.exception.RouteNotFoundException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import fr.gouv.vitamui.commons.rest.RestTestApplication;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@SpringBootTest(classes = RestTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class RestExceptionHandlerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test a VITAMUI Exception that wans't mapped with a key.
     */
    @Test
    public void testVitamUIException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.VITAMUI_EXCEPTION,
            new VitamUIError(),
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertNull(result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a ApplicationServerException and the JSON object in response.
     */
    @Test
    public void testApplicationServerException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.APPLICATION_SERVER_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertNotNull(result.getBody().getTimestamp(), "Timestamp should be defined");
        Assertions.assertEquals(
            ApplicationServerException.class.getName(),
            result.getBody().getException(),
            "Exception should be correctly defined."
        );
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a ApplicationServerException and the JSON object in response.
     */
    @Test
    public void testApplicationServerExceptionWithMessageAndThrowable() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.APPLICATION_SERVER_EXCEPTION_WITH_MESSAGE_AND_THROWABLE,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a BadRequestException and the JSON object in response.
     */
    @Test
    public void testBadRequestException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.BAD_REQUEST_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Spring BindException and the JSON object in response.
     */
    @Test
    public void testBadRequestBindException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.SPRING_BIND_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a BadRequestException and the JSON object in response.
     */
    @Test
    public void testBadRequestExceptionWithThrowable() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.BAD_REQUEST_EXCEPTION_WITH_THROWABLE,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Spring HttpMessageNotReadableException and the JSON object in response.
     */
    @Test
    public void testBadRequestHttpMessageNotReadableException() {
        final VitamUIError vitamuiDto = new VitamUIError();
        vitamuiDto.setArgs(List.of("msg"));
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_POST,
            vitamuiDto,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Unsupported Media Type Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMediaTypeException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_POST_BAD_REQUEST_EXCEPTION,
            "",
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Not Acceptable Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMediaTypeNotAcceptableException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_POST_BAD_REQUEST_EXCEPTION,
            new VitamUIError(),
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.NOT_ACCEPTABLE,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Method Not Allowed Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMethodException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_BAD_REQUEST_EXCEPTION,
            "",
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.METHOD_NOT_ALLOWED,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Method Argument Not Valid Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMethodArgumentNotValidException() {
        final VitamUIError vitamuiError = new VitamUIError();
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_POST,
            vitamuiError,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Missing Servlet Request Parameter Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMissingServletRequestParameterException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.SPRING_BAD_REQUEST_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Missing Servlet Request Part Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMissingServletRequestPartException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.SPRING_MISSING_SERVLET_REQUEST_PART_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Bad Request Parameter Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestParameterException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.SPRING_BAD_REQUEST_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Bad Request Servlet Binding Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestServletRequestBindingException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.SPRING_BAD_REQUEST_EXCEPTION + "?name=1",
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Type Mismatch Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestTypeMismatchException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.SPRING_BAD_REQUEST_EXCEPTION + "?name=name",
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a ForbiddenException and the JSON object in response.
     */
    @Test
    public void testForbiddenException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.FORBIDDEN_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.FORBIDDEN,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ForbiddenException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a InternalServerException and the JSON object in response.
     */
    @Test
    public void testInternalServerException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.INTERNAL_SERVER_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Spring AsyncRequestTimeoutException and the JSON object in response.
     */
    @Test
    public void testInternalServerExceptionAsyncRequestTimeoutException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_ASYNC_REQUEST_TIMEOUT_EXCEPTION,
            new VitamUIError(),
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.SERVICE_UNAVAILABLE,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Spring ConversionNotSupportedException and the JSON object in response.
     */
    @Test
    public void testInternalServerExceptionConversionNotSupportedException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_CONVERSION_NOT_SUPPORTED_EXCEPTION,
            new VitamUIError(),
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Spring SPRING_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION and the JSON object in response.
     */
    @Test
    public void testInternalServerExceptionHttpMessageNotWritableException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION,
            new VitamUIError(),
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a Spring MissingPathVariableException and the JSON object in response.
     */
    @Test
    public void testInternalServerExceptionMissingPathVariableException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(
            TestController.SPRING_MISSING_PATH_VARIABLE_EXCEPTION,
            new VitamUIError(),
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a InvalidAuthenticationException and the JSON object in response.
     */
    @Test
    public void testInvalidAuthenticationException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.INVALID_AUTHENTICATION_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.UNAUTHORIZED,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(InvalidAuthenticationException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a InvalidFormatException and the JSON object in response.
     */
    @Test
    public void testInvalidFormatException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.INVALID_FORMAT_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(InvalidFormatException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a NoRightsException and the JSON object in response.
     */
    @Test
    public void testNoRightsException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.NO_RIGHTS_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.FORBIDDEN,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(NoRightsException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a NotImplementedException and the JSON object in response.
     */
    @Test
    public void testNotImplementedException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.APPLICATION_SERVER_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a ParseOperationException and the JSON object in response.
     */
    @Test
    public void testParseOperationException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.PARSE_OPERATION_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ParseOperationException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a ParseOperationException and the JSON object in response.
     */
    @Test
    public void testParseOperationExceptionWithThrowable() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.PARSE_OPERATION_EXCEPTION_WITH_THROWABLE,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ParseOperationException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a RouteNotFoundException and the JSON object in response.
     */
    @Test
    public void testRouteNotFoundException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.ROUTE_NOT_FOUND_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.NOT_FOUND,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(RouteNotFoundException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a UnAuthorizedException and the JSON object in response.
     */
    @Test
    public void testUnAuthorizedException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.UN_AUTHORIZED_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.UNAUTHORIZED,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(UnAuthorizedException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a ValidationException and the JSON object in response.
     */
    @Test
    public void testValidationException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.VALIDATION_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ValidationException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a IllegalArgumentException and the JSON object in response.
     */
    @Test
    public void testIllegalArgumentException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.ILLEGAL_ARGUMENT_SERVER_EXCEPTION,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.BAD_REQUEST,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }

    /**
     * Test a RequestTimeOutException and the JSON object in response.
     */
    @Test
    public void testRequestTimeOutException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(
            TestController.REQUEST_TIMEOUT_ERROR,
            VitamUIError.class
        );
        Assertions.assertEquals(
            HttpStatus.REQUEST_TIMEOUT,
            result.getStatusCode(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(RequestTimeOutException.class);
        Assertions.assertNotNull(result.getBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(result.getBody().getMessage(), "Exception message should be correctly defined.");
    }
}
