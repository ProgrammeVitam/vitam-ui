package fr.gouv.vitamui.commons.rest.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NoRightsException;
import fr.gouv.vitamui.commons.api.exception.ParseOperationException;
import fr.gouv.vitamui.commons.api.exception.RouteNotFoundException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;
import fr.gouv.vitamui.commons.rest.AbstractRestTest;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.RestTestApplication;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;

/**
 * Test Rest Exception Handler.
 *
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "com.sun.org.apache.xalan.*" })
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RestTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@ActiveProfiles("test")
@PrepareForTest({ RestExceptionHandler.class })
public class RestExceptionHandlerTest extends AbstractRestTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

    @AfterClass
    public static void tearDownAfterClass() {
        System.setErr(out);
    }

    /**
     * Test a VITAMUI Exception that wans't mapped with a key.
     * @throws ParseException
     */
    @Test
    public void testVitamUIException() throws ParseException {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.VITAMUI_EXCEPTION, new VitamUIError(), VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull("Exception informations are empty.", result.getBody());
        assertNull("ExceptionKey should be correctly defined.", result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a ApplicationServerException and the JSON object in response.
     * @throws ParseException
     */
    @Test
    public void testApplicationServerException() throws ParseException {
        // define a fixed date-time
        final OffsetDateTime fixedDateTime = OffsetDateTime.now();

        PowerMockito.mockStatic(OffsetDateTime.class);
        PowerMockito.when(OffsetDateTime.now()).thenReturn(fixedDateTime);

        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.APPLICATION_SERVER_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("Timestamp should be correctly defined.", String.valueOf(fixedDateTime.toEpochSecond()), result.getBody().getTimestamp());
        assertEquals("Exception should be correctly defined.", ApplicationServerException.class.getName(), result.getBody().getException());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a ApplicationServerException and the JSON object in response.
     */
    @Test
    public void testApplicationServerExceptionWithMessageAndThrowable() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.APPLICATION_SERVER_EXCEPTION_WITH_MESSAGE_AND_THROWABLE,
                VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a BadRequestException and the JSON object in response.
     */
    @Test
    public void testBadRequestException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.BAD_REQUEST_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Spring BindException and the JSON object in response.
     */
    @Test
    public void testBadRequestBindException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.SPRING_BIND_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a BadRequestException and the JSON object in response.
     */
    @Test
    public void testBadRequestExceptionWithThrowable() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.BAD_REQUEST_EXCEPTION_WITH_THROWABLE, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Spring HttpMessageNotReadableException and the JSON object in response.
     */
    @Test
    public void testBadRequestHttpMessageNotReadableException() {
        final VitamUIError vitamuiDto = new VitamUIError();
        vitamuiDto.setArgs(Arrays.asList("msg"));
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_POST, vitamuiDto, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Unsupported Media Type Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMediaTypeException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_POST_BAD_REQUEST_EXCEPTION, "", VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.UNSUPPORTED_MEDIA_TYPE, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Not Acceptable Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMediaTypeNotAcceptableException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_POST_BAD_REQUEST_EXCEPTION, new VitamUIError(),
                VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.NOT_ACCEPTABLE, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Method Not Allowed Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMethodException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_BAD_REQUEST_EXCEPTION, "", VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.METHOD_NOT_ALLOWED, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Method Argument Not Valid Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMethodArgumentNotValidException() {
        final VitamUIError vitamuiError = new VitamUIError();
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_POST, vitamuiError, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Missing Servlet Request Parameter Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMissingServletRequestParameterException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.SPRING_BAD_REQUEST_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Missing Servlet Request Part Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestMissingServletRequestPartException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.SPRING_MISSING_SERVLET_REQUEST_PART_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Bad Request Parameter Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestParameterException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.SPRING_BAD_REQUEST_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Bad Request Servlet Binding Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestServletRequestBindingException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.SPRING_BAD_REQUEST_EXCEPTION + "?name=1", VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Type Mismatch Exception and the JSON object in response.
     */
    @Test
    public void testBadRequestTypeMismatchException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.SPRING_BAD_REQUEST_EXCEPTION + "?name=name", VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a ForbiddenException and the JSON object in response.
     */
    @Test
    public void testForbiddenException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.FORBIDDEN_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.FORBIDDEN, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(ForbiddenException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a InternalServerException and the JSON object in response.
     */
    @Test
    public void testInternalServerException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.INTERNAL_SERVER_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Spring AsyncRequestTimeoutException and the JSON object in response.
     */
    @Test
    public void testInternalServerExceptionAsyncRequestTimeoutException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_ASYNC_REQUEST_TIMEOUT_EXCEPTION, new VitamUIError(),
                VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Spring ConversionNotSupportedException and the JSON object in response.
     */
    @Test
    public void testInternalServerExceptionConversionNotSupportedException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_CONVERSION_NOT_SUPPORTED_EXCEPTION, new VitamUIError(),
                VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Spring SPRING_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION and the JSON object in response.
     */
    @Test
    public void testInternalServerExceptionHttpMessageNotWritableException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION, new VitamUIError(),
                VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a Spring MissingPathVariableException and the JSON object in response.
     */
    @Test
    public void testInternalServerExceptionMissingPathVariableException() {
        final ResponseEntity<VitamUIError> result = restTemplate.postForEntity(TestController.SPRING_MISSING_PATH_VARIABLE_EXCEPTION, new VitamUIError(),
                VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(InternalServerException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a InvalidAuthenticationException and the JSON object in response.
     */
    @Test
    public void testInvalidAuthenticationException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.INVALID_AUTHENTICATION_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.UNAUTHORIZED, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(InvalidAuthenticationException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a InvalidFormatException and the JSON object in response.
     */
    @Test
    public void testInvalidFormatException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.INVALID_FORMAT_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(InvalidFormatException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a NoRightsException and the JSON object in response.
     */
    @Test
    public void testNoRightsException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.NO_RIGHTS_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.FORBIDDEN, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(NoRightsException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a NotImplementedException and the JSON object in response.
     */
    @Test
    public void testNotImplementedException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.APPLICATION_SERVER_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(ApplicationServerException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a ParseOperationException and the JSON object in response.
     */
    @Test
    public void testParseOperationException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.PARSE_OPERATION_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(ParseOperationException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a ParseOperationException and the JSON object in response.
     * @throws Exception
     */
    @Test
    public void testParseOperationExceptionWithThrowable() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.PARSE_OPERATION_EXCEPTION_WITH_THROWABLE, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(ParseOperationException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a RouteNotFoundException and the JSON object in response.
     */
    @Test
    public void testRouteNotFoundException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.ROUTE_NOT_FOUND_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.NOT_FOUND, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(RouteNotFoundException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a UnAuthorizedException and the JSON object in response.
     */
    @Test
    public void testUnAuthorizedException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.UN_AUTHORIZED_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.UNAUTHORIZED, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(UnAuthorizedException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a ValidationException and the JSON object in response.
     */
    @Test
    public void testValidationException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.VALIDATION_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(ValidationException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

    /**
     * Test a IllegalArgumentException and the JSON object in response.
     */
    @Test
    public void testIllegalArgumentException() {
        final ResponseEntity<VitamUIError> result = restTemplate.getForEntity(TestController.ILLEGAL_ARGUMENT_SERVER_EXCEPTION, VitamUIError.class);
        assertEquals("Status code should be correctly defined.", HttpStatus.BAD_REQUEST, result.getStatusCode());
        final String key = ApiErrorGenerator.buildKey(BadRequestException.class);
        assertNotNull("Exception informations are empty.", result.getBody());
        assertEquals("ExceptionKey should be correctly defined.", key, result.getBody().getError());
        assertNotNull("Exception message should be correctly defined.", result.getBody().getMessage());
    }

}
