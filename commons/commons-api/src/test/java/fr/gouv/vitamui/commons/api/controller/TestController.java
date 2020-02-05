package fr.gouv.vitamui.commons.api.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NoRightsException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.exception.ParseOperationException;
import fr.gouv.vitamui.commons.api.exception.RouteNotFoundException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;

@RestController
public class TestController {

    public static final String APPLICATION_SERVER_EXCEPTION = "/test/applicationServerException";

    public static final String APPLICATION_SERVER_EXCEPTION_WITH_THROWABLE = "/test/applicationServerExceptionWithThrowable";

    public static final String APPLICATION_SERVER_EXCEPTION_WITH_MESSAGE_AND_THROWABLE = "/test/applicationServerExceptionWithMessageAndThrowable";

    public static final String BAD_REQUEST_EXCEPTION = "/test/badRequestException";

    public static final String BAD_REQUEST_EXCEPTION_WITH_THROWABLE = "/test/badRequestExceptionWithThrowable";

    public static final String FORBIDDEN_EXCEPTION = "/test/forbiddenException";

    public static final String INTERNAL_SERVER_EXCEPTION = "/test/internalServerException";

    public static final String INVALID_AUTHENTICATION_EXCEPTION = "/test/invalidAuthenticationException";

    public static final String INVALID_FORMAT_EXCEPTION = "/test/invalidFormatException";

    public static final String NOT_FOUND_EXCEPTION = "/test/notFoundException";

    public static final String NO_RIGHTS_EXCEPTION = "/test/noRightsException";

    public static final String NOT_IMPLEMENTED_EXCEPTION = "/test/notImplementedException";

    public static final String PARSE_OPERATION_EXCEPTION = "/test/parseOperationException";

    public static final String PARSE_OPERATION_EXCEPTION_WITH_THROWABLE = "/test/parseOperationExceptionWithThrowable";

    public static final String ROUTE_NOT_FOUND_EXCEPTION = "/test/routeNotFoundException";

    public static final String UN_AUTHORIZED_EXCEPTION = "/test/unAuthorizedException";

    public static final String VALIDATION_EXCEPTION = "/test/validationException";

    @RequestMapping(value = APPLICATION_SERVER_EXCEPTION)
    public String getApplicationServerException() {
        throw new ApplicationServerException("Message");
    }

    @RequestMapping(value = APPLICATION_SERVER_EXCEPTION_WITH_MESSAGE_AND_THROWABLE)
    public String getApplicationServerExceptionWithMessageAndThrowable() {
        throw new ApplicationServerException("Message", new IOException());
    }

    @RequestMapping(value = APPLICATION_SERVER_EXCEPTION_WITH_THROWABLE)
    public String getApplicationServerExceptionWithThrowable() {
        throw new ApplicationServerException(new IOException());
    }

    @RequestMapping(value = BAD_REQUEST_EXCEPTION)
    public String getBadRequestException() {
        throw new BadRequestException("BadRequestException");
    }

    @RequestMapping(value = BAD_REQUEST_EXCEPTION_WITH_THROWABLE)
    public String getBadRequestExceptionWithThrowable() {
        throw new BadRequestException("BadRequestException", new IOException());
    }

    @RequestMapping(value = FORBIDDEN_EXCEPTION)
    public String getForbiddenException() {
        throw new ForbiddenException("ForbiddenException");
    }

    @RequestMapping(value = INTERNAL_SERVER_EXCEPTION)
    public String getInternalServerException() {
        throw new InternalServerException("InternalServerException");
    }

    @RequestMapping(value = INVALID_AUTHENTICATION_EXCEPTION)
    public String getInvalidAuthenticationException() {
        throw new InvalidAuthenticationException("InvalidAuthenticationException");
    }

    @RequestMapping(value = INVALID_FORMAT_EXCEPTION)
    public String getInvalidFormatException() {
        throw new InvalidFormatException("InvalidFormatException");
    }

    @RequestMapping(value = NO_RIGHTS_EXCEPTION)
    public String getNoRightsException() {
        throw new NoRightsException("NoRightsException");
    }

    @RequestMapping(value = NOT_FOUND_EXCEPTION)
    public String getNotFoundException() {
        throw new NotFoundException("NotFoundException");
    }

    @RequestMapping(value = NOT_IMPLEMENTED_EXCEPTION)
    public String getNotImplementedException() {
        throw new NotImplementedException("NotImplementedException");
    }

    @RequestMapping(value = PARSE_OPERATION_EXCEPTION)
    public String getParseOperationException() {
        throw new ParseOperationException("ParseOperationException");
    }

    @RequestMapping(value = PARSE_OPERATION_EXCEPTION_WITH_THROWABLE)
    public String getParseOperationExceptionWithThrowable() {
        throw new ParseOperationException("ParseOperationException");
    }

    @RequestMapping(value = ROUTE_NOT_FOUND_EXCEPTION)
    public String getRouteNotFoundException() {
        throw new RouteNotFoundException("RouteNotFoundException");
    }

    @RequestMapping(value = UN_AUTHORIZED_EXCEPTION)
    public String getUnAuthorizedException() {
        throw new UnAuthorizedException("UnAuthorizedException");
    }

    @RequestMapping(value = VALIDATION_EXCEPTION)
    public String getValidationException() {
        throw new ValidationException("ValidationException");
    }

}
