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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

@SpringBootTest(classes = RestTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureRestTestClient
public class RestExceptionHandlerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTestClient restTestClient;

    /**
     * Test a VITAMUI Exception that wans't mapped with a key.
     */
    @Test
    void testVitamUIException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.VITAMUI_EXCEPTION)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new VitamUIError())
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(VitamUIError.class)
            .returnResult();

        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertNull(result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a ApplicationServerException and the JSON object in response.
     */
    @Test
    void testApplicationServerException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.APPLICATION_SERVER_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(VitamUIError.class)
            .returnResult();
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            result.getStatus(),
            "Status code should be correctly defined."
        );
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertNotNull(result.getResponseBody().getTimestamp(), "Timestamp should be defined");
        Assertions.assertEquals(
            ApplicationServerException.class.getName(),
            result.getResponseBody().getException(),
            "Exception should be correctly defined."
        );
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a ApplicationServerException and the JSON object in response.
     */
    @Test
    void testApplicationServerExceptionWithMessageAndThrowable() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.APPLICATION_SERVER_EXCEPTION_WITH_MESSAGE_AND_THROWABLE)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a BadRequestException and the JSON object in response.
     */
    @Test
    void testBadRequestException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.BAD_REQUEST_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Spring BindException and the JSON object in response.
     */
    @Test
    void testBadRequestBindException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.SPRING_BIND_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a BadRequestException and the JSON object in response.
     */
    @Test
    void testBadRequestExceptionWithThrowable() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.BAD_REQUEST_EXCEPTION_WITH_THROWABLE)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Spring HttpMessageNotReadableException and the JSON object in response.
     */
    @Test
    void testBadRequestHttpMessageNotReadableException() {
        final VitamUIError vitamuiDto = new VitamUIError();
        vitamuiDto.setArgs(List.of("msg"));
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.SPRING_POST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("invalid json")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Unsupported Media Type Exception and the JSON object in response.
     */
    @Test
    void testBadRequestMediaTypeException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.SPRING_POST_BAD_REQUEST_EXCEPTION)
            .contentType(MediaType.APPLICATION_XML)
            .body("<xml/>")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Not Acceptable Exception and the JSON object in response.
     */
    @Test
    void testBadRequestMediaTypeNotAcceptableException() {
        restTestClient
            .post()
            .uri(TestController.SPRING_POST_BAD_REQUEST_EXCEPTION)
            .accept(MediaType.APPLICATION_XML)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new VitamUIError())
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Test a Method Not Allowed Exception and the JSON object in response.
     */
    @Test
    void testBadRequestMethodException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.SPRING_BAD_REQUEST_EXCEPTION)
            .body(new VitamUIError())
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Method Argument Not Valid Exception and the JSON object in response.
     */
    @Test
    void testBadRequestMethodArgumentNotValidException() {
        final VitamUIError vitamuiError = new VitamUIError();
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.SPRING_POST)
            .body(new VitamUIError())
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Missing Servlet Request Parameter Exception and the JSON object in response.
     */
    @Test
    void testBadRequestMissingServletRequestParameterException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.SPRING_BAD_REQUEST_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Missing Servlet Request Part Exception and the JSON object in response.
     */
    @Test
    void testBadRequestMissingServletRequestPartException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.SPRING_MISSING_SERVLET_REQUEST_PART_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Bad Request Parameter Exception and the JSON object in response.
     */
    @Test
    void testBadRequestParameterException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.SPRING_BAD_REQUEST_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Bad Request Servlet Binding Exception and the JSON object in response.
     */
    @Test
    void testBadRequestServletRequestBindingException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.SPRING_BAD_REQUEST_EXCEPTION + "?name=1")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Type Mismatch Exception and the JSON object in response.
     */
    @Test
    void testBadRequestTypeMismatchException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.SPRING_BAD_REQUEST_EXCEPTION + "?name=1")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a ForbiddenException and the JSON object in response.
     */
    @Test
    void testForbiddenException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.FORBIDDEN_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.FORBIDDEN)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(ForbiddenException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a InternalServerException and the JSON object in response.
     */
    @Test
    void testInternalServerException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.INTERNAL_SERVER_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Spring AsyncRequestTimeoutException and the JSON object in response.
     */
    @Test
    void testInternalServerExceptionAsyncRequestTimeoutException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.SPRING_ASYNC_REQUEST_TIMEOUT_EXCEPTION)
            .body(new VitamUIError())
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Spring ConversionNotSupportedException and the JSON object in response.
     */
    @Test
    void testInternalServerExceptionConversionNotSupportedException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.SPRING_CONVERSION_NOT_SUPPORTED_EXCEPTION)
            .body(new VitamUIError())
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Spring SPRING_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION and the JSON object in response.
     */
    @Test
    void testInternalServerExceptionHttpMessageNotWritableException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.SPRING_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION)
            .body(new VitamUIError())
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a Spring MissingPathVariableException and the JSON object in response.
     */
    @Test
    void testInternalServerExceptionMissingPathVariableException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .post()
            .uri(TestController.SPRING_MISSING_PATH_VARIABLE_EXCEPTION)
            .body(new VitamUIError())
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a InvalidAuthenticationException and the JSON object in response.
     */
    @Test
    void testInvalidAuthenticationException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.INVALID_AUTHENTICATION_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.UNAUTHORIZED)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(InvalidAuthenticationException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a InvalidFormatException and the JSON object in response.
     */
    @Test
    void testInvalidFormatException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.INVALID_FORMAT_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(InvalidFormatException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a NoRightsException and the JSON object in response.
     */
    @Test
    void testNoRightsException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.NO_RIGHTS_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.FORBIDDEN)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(NoRightsException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a NotImplementedException and the JSON object in response.
     */
    @Test
    void testNotImplementedException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.APPLICATION_SERVER_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a ParseOperationException and the JSON object in response.
     */
    @Test
    void testParseOperationException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.PARSE_OPERATION_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(ParseOperationException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a ParseOperationException and the JSON object in response.
     */
    @Test
    void testParseOperationExceptionWithThrowable() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.PARSE_OPERATION_EXCEPTION_WITH_THROWABLE)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(ParseOperationException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a RouteNotFoundException and the JSON object in response.
     */
    @Test
    void testRouteNotFoundException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.ROUTE_NOT_FOUND_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(RouteNotFoundException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a UnAuthorizedException and the JSON object in response.
     */
    @Test
    void testUnAuthorizedException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.UN_AUTHORIZED_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.UNAUTHORIZED)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(UnAuthorizedException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a ValidationException and the JSON object in response.
     */
    @Test
    void testValidationException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.VALIDATION_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(ValidationException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a IllegalArgumentException and the JSON object in response.
     */
    @Test
    void testIllegalArgumentException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.ILLEGAL_ARGUMENT_SERVER_EXCEPTION)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }

    /**
     * Test a RequestTimeOutException and the JSON object in response.
     */
    @Test
    void testRequestTimeOutException() {
        final EntityExchangeResult<VitamUIError> result = restTestClient
            .get()
            .uri(TestController.REQUEST_TIMEOUT_ERROR)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.REQUEST_TIMEOUT)
            .expectBody(VitamUIError.class)
            .returnResult();

        final String key = ApiErrorGenerator.buildKey(RequestTimeOutException.class);
        Assertions.assertNotNull(result.getResponseBody(), "Exception informations are empty.");
        Assertions.assertEquals(key, result.getResponseBody().getError(), "ExceptionKey should be correctly defined.");
        Assertions.assertNotNull(
            result.getResponseBody().getMessage(),
            "Exception message should be correctly defined."
        );
    }
}
