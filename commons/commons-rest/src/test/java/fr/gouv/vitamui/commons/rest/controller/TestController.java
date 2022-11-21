package fr.gouv.vitamui.commons.rest.controller;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.validation.Valid;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.exception.ParseOperationException;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import fr.gouv.vitamui.commons.rest.dto.VitamUIDto;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;

@RestController
public class TestController {

    public static final String VITAMUI_EXCEPTION = "/test/vitamuiException";

    public static final String APPLICATION_SERVER_EXCEPTION = "/test/applicationServerException";

    public static final String APPLICATION_SERVER_EXCEPTION_WITH_THROWABLE = "/test/applicationServerExceptionWithThrowable";

    public static final String APPLICATION_SERVER_EXCEPTION_WITH_MESSAGE_AND_THROWABLE = "/test/applicationServerExceptionWithMessageAndThrowable";

    public static final String BAD_REQUEST_EXCEPTION = "/test/badRequestException";

    public static final String BAD_REQUEST_EXCEPTION_WITH_THROWABLE = "/test/badRequestExceptionWithThrowable";

    public static final String FORBIDDEN_EXCEPTION = "/test/forbiddenException";

    public static final String INTERNAL_SERVER_EXCEPTION = "/test/internalServerException";

    public static final String ILLEGAL_ARGUMENT_SERVER_EXCEPTION = "/test/IllegalArgumentException";

    public static final String INVALID_AUTHENTICATION_EXCEPTION = "/test/invalidAuthenticationException";

    public static final String INVALID_FORMAT_EXCEPTION = "/test/invalidFormatException";

    public static final String NO_RIGHTS_EXCEPTION = "/test/noRightsException";

    public static final String NOT_IMPLEMENTED_EXCEPTION = "/test/notImplementedException";

    public static final String PARSE_OPERATION_EXCEPTION = "/test/parseOperationException";

    public static final String PARSE_OPERATION_EXCEPTION_WITH_THROWABLE = "/test/parseOperationExceptionWithThrowable";

    public static final String ROUTE_NOT_FOUND_EXCEPTION = "/test/unfound";

    public static final String SPRING_BAD_REQUEST_EXCEPTION = "/test/springtBadRequestException";

    public static final String SPRING_POST_BAD_REQUEST_EXCEPTION = "/test/postSpringtBadRequestException";

    public static final String SPRING_POST_BAD_REQUEST_ID_EXCEPTION = "/test/postSpringtBadRequestException/{id}";

    public static final String SPRING_POST = "/test/postSpring";

    public static final String SPRING_ASYNC_REQUEST_TIMEOUT_EXCEPTION = "/test/asyncRequestTimeoutException";

    public static final String SPRING_BIND_EXCEPTION = "/test/bindException";

    public static final String SPRING_CONVERSION_NOT_SUPPORTED_EXCEPTION = "/test/conversionNotSupportedException";

    public static final String SPRING_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION = "/test/httpMessageNotWritableException";

    public static final String SPRING_MISSING_PATH_VARIABLE_EXCEPTION = "/test/missingPathVariableException";

    public static final String SPRING_MISSING_SERVLET_REQUEST_PART_EXCEPTION = "/test/missingServletRequestPartException";

    public static final String UN_AUTHORIZED_EXCEPTION = "/test/unAuthorizedException";

    public static final String VALIDATION_EXCEPTION = "/test/validationException";

    @RequestMapping(value = VITAMUI_EXCEPTION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String vitamuiException(@RequestBody final VitamUIDto name) {
        throw new VitamUIException("Test") {

            /**
             *
             */
            private static final long serialVersionUID = -1360378588671762075L;
        };
    }

    @RequestMapping(value = APPLICATION_SERVER_EXCEPTION)
    public String getApplicationServerException() {
        throw ApiErrorGenerator.getApplicationServerException();
    }

    @RequestMapping(value = APPLICATION_SERVER_EXCEPTION_WITH_MESSAGE_AND_THROWABLE)
    public String getApplicationServerExceptionWithMessageAndThrowable() {
        throw new ApplicationServerException("Message", new IOException());
    }

    @RequestMapping(value = BAD_REQUEST_EXCEPTION)
    public String getBadRequestException() {
        throw ApiErrorGenerator.getBadRequestException("wrong client");
    }

    @RequestMapping(value = BAD_REQUEST_EXCEPTION_WITH_THROWABLE)
    public String getBadRequestExceptionWithThrowable() {
        throw new BadRequestException("BadRequestException", new IOException());
    }

    @RequestMapping(value = FORBIDDEN_EXCEPTION)
    public String getForbiddenException() {
        throw ApiErrorGenerator.getForbiddenException();
    }

    @RequestMapping(value = INTERNAL_SERVER_EXCEPTION)
    public String getInternalServerException() {
        throw ApiErrorGenerator.getInternalServerException();
    }

    @RequestMapping(value = ILLEGAL_ARGUMENT_SERVER_EXCEPTION)
    public void getIllegalArgumentException() {
        Assert.isTrue(false,"Conditions false");
    }


    @RequestMapping(value = INVALID_AUTHENTICATION_EXCEPTION)
    public String getInvalidAuthenticationException() {
        throw ApiErrorGenerator.getInvalidAuthentificationException();
    }

    @RequestMapping(value = INVALID_FORMAT_EXCEPTION)
    public String getInvalidFormatException() {
        throw ApiErrorGenerator.getInvalidFormatException("Invalid format.");
    }

    @RequestMapping(value = NO_RIGHTS_EXCEPTION)
    public String getNoRightsException() {
        throw ApiErrorGenerator.getNoRightsException();
    }

    @RequestMapping(value = NOT_IMPLEMENTED_EXCEPTION)
    public String getNotImplementedException() {
        throw ApiErrorGenerator.getNotImplementedException();
    }

    @RequestMapping(value = PARSE_OPERATION_EXCEPTION)
    public String getParseOperationException() throws ParseOperationException {
        throw ApiErrorGenerator.getParseOperationException("operation is invalid.");
    }

    @RequestMapping(value = PARSE_OPERATION_EXCEPTION_WITH_THROWABLE)
    public String getParseOperationExceptionWithThrowable() {
        throw new ParseOperationException("ParseOperationException");
    }

    @RequestMapping(value = ROUTE_NOT_FOUND_EXCEPTION)
    public String getRouteNotFoundException() {
        throw ApiErrorGenerator.getRouteNotFoundException(HttpMethod.POST, "/path");
    }

    @RequestMapping(value = SPRING_BAD_REQUEST_EXCEPTION, method = RequestMethod.GET)
    public String springBadRequest(@RequestParam(value = "name", required = true) final Integer name,
            @RequestHeader(value = "myheader") final String myheader) {
        return "";
    }

    @RequestMapping(value = SPRING_POST_BAD_REQUEST_EXCEPTION, produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String springBadRequestPost(@RequestBody final VitamUIError name) {
        return "";
    }

    @RequestMapping(SPRING_POST_BAD_REQUEST_ID_EXCEPTION)
    public @ResponseBody String springBadRequestIdPost(@RequestBody final VitamUIError name) {
        return "";
    }

    @RequestMapping(value = SPRING_POST, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String springPost(@Valid @RequestBody final VitamUIDto name) {
        return "";
    }

    @RequestMapping(SPRING_ASYNC_REQUEST_TIMEOUT_EXCEPTION)
    public @ResponseBody String springAsyncRequestTimeoutException() {
        throw new AsyncRequestTimeoutException();
    }

    @RequestMapping(SPRING_BIND_EXCEPTION)
    public @ResponseBody String springBindException() throws BindException {
        throw new BindException(new BeanPropertyBindingResult(new VitamUIDto(), "objectName"));
    }

    @RequestMapping(SPRING_CONVERSION_NOT_SUPPORTED_EXCEPTION)
    public @ResponseBody String springConversionNotSupportedException(@RequestBody final VitamUIError name) {
        throw new ConversionNotSupportedException(new VitamUIDto(), VitamUIDto.class, new IllegalArgumentException("test"));
    }

    @RequestMapping(SPRING_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION)
    public @ResponseBody String springHttpMessageNotWritableException(@RequestBody final VitamUIError name) {
        throw new HttpMessageNotWritableException("message");
    }

    @RequestMapping(SPRING_MISSING_PATH_VARIABLE_EXCEPTION)
    public @ResponseBody String springMissingPathVariableException()
            throws MissingPathVariableException, NoSuchMethodException, SecurityException {
        final Method testMethod = getClass().getDeclaredMethod("toString");
        throw new MissingPathVariableException("", new MethodParameter(testMethod, -1));
    }

    @RequestMapping(SPRING_MISSING_SERVLET_REQUEST_PART_EXCEPTION)
    public @ResponseBody String springMissingServletRequestPartException() throws MissingServletRequestPartException {
        throw new MissingServletRequestPartException("partName");
    }

    @RequestMapping(value = UN_AUTHORIZED_EXCEPTION)
    public String getUnAuthorizedException() {
        throw ApiErrorGenerator.getUnAuthorizedException();
    }

    @RequestMapping(value = VALIDATION_EXCEPTION)
    public String getValidationException() {
        throw ApiErrorGenerator.getValidationException();
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
