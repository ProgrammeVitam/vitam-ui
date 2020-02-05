/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final VitamUILogger LOG = VitamUILoggerFactory.getInstance(RestExceptionHandler.class);

    @Autowired
    public RestExceptionHandler() {
        super();
    }

    /**
     * Write the Exception to the Response with VITAMUI customization.
     * @param request
     * @param response
     * @param ex
     * @throws IOException
     * @throws ServletException
     */
    public void writeExceptionToResponse(final HttpServletRequest request, final HttpServletResponse response, final Exception ex)
            throws IOException, ServletException {
        // retrieve the right vitamuiException that we want
        VitamUIException vitamuiException = null;
        if (ex instanceof VitamUIException) {
            vitamuiException = (VitamUIException) ex;
        }
        if (ex instanceof IllegalArgumentException) {
            vitamuiException = new BadRequestException(ex.getMessage(), ex);
        }
        if (ex instanceof AccessDeniedException) {
            vitamuiException = new ForbiddenException(ex.getMessage(), ex);
        }
        if (ex instanceof AuthenticationException) {
            vitamuiException = new InvalidAuthenticationException(ex.getMessage(), ex);
        }
        logException(vitamuiException, request);
        final VitamUIError apiErrors = buildApiErrors(vitamuiException);

        final HttpStatus status = getExceptionStatus(vitamuiException);
        response.setStatus(status.value());

        // write exception data to response
        final OutputStream out = response.getOutputStream();
        final com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, apiErrors);
        out.flush();
    }

    /**
     * Handle VitamUI Exceptions.
     * @param ex
     * @return
     */
    @ExceptionHandler(VitamUIException.class)
    protected ResponseEntity<Object> handleVitamUIException(final VitamUIException ex, final WebRequest request) {
        logException(ex, request);
        return buildResponseEntity(ex);
    }

    /**
     * Handle Other Java Exceptions.
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleOtherException(final Exception ex, final WebRequest request) {
        if (ex instanceof VitamUIException) {
            return handleVitamUIException((VitamUIException) ex, request);
        }
        if (ex instanceof IllegalArgumentException) {
            return handleVitamUIException(new BadRequestException(ex.getMessage(), ex), request);
        }
        if (ex instanceof AccessDeniedException) {
            return handleVitamUIException(new ForbiddenException(ex.getMessage(), ex), request);
        }
        if (ex instanceof AuthenticationException) {
            return handleVitamUIException(new InvalidAuthenticationException(ex.getMessage(), ex), request);
        }
        logException(ex, request);
        final VitamUIError apiErrors = buildApiErrors(ex);

        final HttpStatus status = getExceptionStatus(ex);
        return new ResponseEntity<>(apiErrors, new HttpHeaders(), status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body, final HttpHeaders headers, final HttpStatus status,
            final WebRequest request) {
        VitamUIException vitamuiException = null;
        LOG.error("Handling error : {} : {}.\n{}\nCause:\n{}", ex.getClass().getName(), ex.getMessage(), buildStacktrace(ex), ex.getCause());
        if (ex instanceof MissingPathVariableException || ex instanceof ConversionNotSupportedException || ex instanceof HttpMessageNotWritableException) {
            vitamuiException = ApiErrorGenerator.getInternalServerException(ex.getMessage());
        }
        else if (ex instanceof AsyncRequestTimeoutException) {
            vitamuiException = ApiErrorGenerator.getApplicationServerException(ex.getMessage());
        }
        else if (ex instanceof HttpRequestMethodNotSupportedException || ex instanceof HttpMediaTypeNotSupportedException
                || ex instanceof HttpMediaTypeNotAcceptableException || ex instanceof MissingServletRequestParameterException
                || ex instanceof ServletRequestBindingException || ex instanceof TypeMismatchException || ex instanceof HttpMessageNotReadableException
                || ex instanceof MethodArgumentNotValidException || ex instanceof MissingServletRequestPartException || ex instanceof BindException) {
            vitamuiException = ApiErrorGenerator.getBadRequestException(ex.getMessage());
        }
        else {
            vitamuiException = ApiErrorGenerator.getInternalServerException(ex.getMessage());
        }
        logException(vitamuiException, request);
        return new ResponseEntity<>(buildApiErrors(vitamuiException), headers, status);
    }

    /**
     * Build Response for VitamUIExceptions.
     * @param vitamuiException
     * @return
     */
    private ResponseEntity<Object> buildResponseEntity(final VitamUIException vitamuiException) {
        final VitamUIError apiErrors = buildApiErrors(vitamuiException);

        final HttpStatus status = getExceptionStatus(vitamuiException);
        return new ResponseEntity<>(apiErrors, new HttpHeaders(), status);
    }

    /**
     * Build VITAMUI Exception informations that will be returned to the caller.
     * @param vitamuiException
     * @return
     */
    private VitamUIError buildApiErrors(final VitamUIException vitamuiException) {
        final VitamUIError apiErrors = new VitamUIError();

        final HttpStatus status = getExceptionStatus(vitamuiException);
        apiErrors.setTimestamp("" + OffsetDateTime.now().toEpochSecond());
        apiErrors.setStatus(status.value());
        if (StringUtils.isEmpty(vitamuiException.getKey())) {
            apiErrors.setError(ApiErrorGenerator.buildKey(vitamuiException.getClass()));
        }
        else {
            apiErrors.setError(vitamuiException.getKey());
        }

        apiErrors.setException(vitamuiException.getClass().getName());
        apiErrors.setMessage(vitamuiException.getMessage());
        if (vitamuiException.getArgs() != null) {
            apiErrors.setArgs(vitamuiException.getArgs());
        }

        return apiErrors;
    }

    /**
     * Build Exception informations that will be returned to the caller.
     * @param exception
     * @return
     */
    private VitamUIError buildApiErrors(final Exception exception) {
        final VitamUIError apiErrors = new VitamUIError();

        final HttpStatus status = getExceptionStatus(exception);
        apiErrors.setTimestamp("" + (new Date()).getTime());
        apiErrors.setStatus(status.value());
        apiErrors.setError(ApiErrorGenerator.buildKey(InternalServerException.class));

        apiErrors.setException(exception.getClass().getName());
        apiErrors.setMessage(exception.getMessage());

        return apiErrors;
    }

    /**
     * Get Exception Status.
     * @param exception
     * @return
     */
    private HttpStatus getExceptionStatus(final Exception exception) {
        HttpStatus status = null;
        final ResponseStatus responseStatus = exception.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            status = responseStatus.value();
        }
        else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return status;
    }

    /**
     * Log Exception informations.
     * @param exception
     * @param request
     */
    private void logException(final Exception exception, final HttpServletRequest request) {
        final HttpStatus status = getExceptionStatus(exception);
        final StringBuilder stackTrace = new StringBuilder();
        if (exception.getStackTrace() != null) {
            for (final StackTraceElement element : exception.getStackTrace()) {
                stackTrace.append("\t at " + element.toString() + "\n");
            }
        }
        LOG.error("Error during API call {} {}. \nStatusCode : {}. \n{} : {}.\n{}\nCause:\n", request.getMethod(), request.getServletPath(), status.value(),
                exception.getClass().getName(), exception.getMessage(), stackTrace.toString(), exception.getCause());
    }

    /**
     * Log Exception informations.
     * @param exception
     * @param request
     */
    private void logException(final Exception exception, final WebRequest request) {
        final ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        final HttpStatus status = getExceptionStatus(exception);
        final String stackTrace = buildStacktrace(exception);
        LOG.error("Error during API call {} {}. \nStatusCode : {}. \n{} : {}.\n{}\nCause:\n{}", servletWebRequest.getHttpMethod(),
                servletWebRequest.getRequest().getServletPath(), status.value(), exception.getClass().getName(), exception.getMessage(), stackTrace,
                exception.getCause());
    }

    private String buildStacktrace(final Exception exception) {
        final StringBuilder stackTrace = new StringBuilder();
        if (exception.getStackTrace() != null) {
            for (final StackTraceElement element : exception.getStackTrace()) {
                stackTrace.append("\t at " + element.toString() + "\n");
            }
        }
        return stackTrace.toString();
    }

}
