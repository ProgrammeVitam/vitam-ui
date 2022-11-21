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

import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.FileGenerationException;
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
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;

/**
 * API Error Generator.
 *
 *
 */
public final class ApiErrorGenerator {

    private static Map<Class<?>, String> keysMappings = initializeKeysMappings();

    private static final Map<String, String> apiErrorsMessages = initializeApiErrors();

    private ApiErrorGenerator() {
        // do nothing
    }

    /**
     * Initialize Errors Configuration
     */
    private static Map<String, String> initializeApiErrors() {
        final Map<String, String> mappings = new HashMap<>();

        mappings.put(ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR, ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_BAD_REQUEST, ErrorsConstants.API_ERRORS_BAD_REQUEST_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_UNAUTHORIZED, ErrorsConstants.API_ERRORS_UNAUTHORIZED_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR, ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_UNAUTHORIZED, ErrorsConstants.API_ERRORS_UNAUTHORIZED_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_BAD_FORMAT, ErrorsConstants.API_ERRORS_BAD_FORMAT_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_NO_RIGHTS, ErrorsConstants.API_ERRORS_NO_RIGHTS_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_NOTFOUND, ErrorsConstants.API_ERRORS_NOTFOUND_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_ROUTE_NOT_IMPLEMENTED_YET, ErrorsConstants.API_ERRORS_ROUTE_NOT_IMPLEMENTED_YET_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_BAD_REQUEST, ErrorsConstants.API_ERRORS_BAD_REQUEST_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_ROUTE_NOT_FOUND, ErrorsConstants.API_ERRORS_ROUTE_NOT_FOUND_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_UNAUTHORIZED, ErrorsConstants.API_ERRORS_UNAUTHORIZED_MESSAGE);
        mappings.put(ErrorsConstants.API_ERRORS_VALIDATION, ErrorsConstants.API_ERRORS_VALIDATION_MESSAGE);
        return mappings;
    }

    /**
     * Initialize keys mappings.
     */
    private static Map<Class<?>, String> initializeKeysMappings() {
        final Map<Class<?>, String> keysMappings = new HashMap<>();

        keysMappings.put(ApplicationServerException.class, ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR);
        keysMappings.put(BadRequestException.class, ErrorsConstants.API_ERRORS_BAD_REQUEST);
        keysMappings.put(ConflictException.class, ErrorsConstants.API_ERRORS_BAD_REQUEST);
        keysMappings.put(UnexpectedDataException.class, ErrorsConstants.API_ERRORS_BAD_REQUEST);
        keysMappings.put(FileGenerationException.class, ErrorsConstants.API_ERRORS_BAD_REQUEST);
        keysMappings.put(ForbiddenException.class, ErrorsConstants.API_ERRORS_UNAUTHORIZED);
        keysMappings.put(InternalServerException.class, ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR);
        keysMappings.put(InvalidAuthenticationException.class, ErrorsConstants.API_ERRORS_UNAUTHORIZED);
        keysMappings.put(InvalidFormatException.class, ErrorsConstants.API_ERRORS_BAD_FORMAT);
        keysMappings.put(NoRightsException.class, ErrorsConstants.API_ERRORS_NO_RIGHTS);
        keysMappings.put(NotFoundException.class, ErrorsConstants.API_ERRORS_NOTFOUND);
        keysMappings.put(NotImplementedException.class, ErrorsConstants.API_ERRORS_ROUTE_NOT_IMPLEMENTED_YET);
        keysMappings.put(ParseOperationException.class, ErrorsConstants.API_ERRORS_BAD_REQUEST);
        keysMappings.put(RouteNotFoundException.class, ErrorsConstants.API_ERRORS_ROUTE_NOT_FOUND);
        keysMappings.put(UnAuthorizedException.class, ErrorsConstants.API_ERRORS_UNAUTHORIZED);
        keysMappings.put(ValidationException.class, ErrorsConstants.API_ERRORS_VALIDATION);
        return keysMappings;
    }

    /**
     * @return the invalidAuthentificationException
     */
    public static InvalidAuthenticationException getInvalidAuthentificationException(final Object... args) {
        return new InvalidAuthenticationException(getMessage(ErrorsConstants.API_ERRORS_UNAUTHORIZED, args), buildKey(ErrorsConstants.API_ERRORS_UNAUTHORIZED));
    }

    /**
     * @return the noRightsException
     */
    public static NoRightsException getNoRightsException(final Object... args) {
        return new NoRightsException(getMessage(ErrorsConstants.API_ERRORS_NO_RIGHTS, args), buildKey(ErrorsConstants.API_ERRORS_NO_RIGHTS));
    }

    /**
     * @return the badRequestException
     */
    public static BadRequestException getBadRequestException(final Object... args) {
        return new BadRequestException(getMessage(ErrorsConstants.API_ERRORS_BAD_REQUEST, args), buildKey(ErrorsConstants.API_ERRORS_BAD_REQUEST));
    }

    /**
     * @return the invalidFormatException
     */
    public static InvalidFormatException getInvalidFormatException(final Object... args) {
        return new InvalidFormatException(getMessage(ErrorsConstants.API_ERRORS_BAD_FORMAT, args), buildKey(ErrorsConstants.API_ERRORS_BAD_FORMAT));
    }

    /**
     * @return the forbiddenException
     */
    public static ForbiddenException getForbiddenException(final Object... args) {
        return new ForbiddenException(getMessage(ErrorsConstants.API_ERRORS_UNAUTHORIZED, args), buildKey(ErrorsConstants.API_ERRORS_UNAUTHORIZED));
    }

    /**
     * @return the unAuthorizedException
     */
    public static UnAuthorizedException getUnAuthorizedException(final Object... args) {
        return new UnAuthorizedException(getMessage(ErrorsConstants.API_ERRORS_UNAUTHORIZED, args), buildKey(ErrorsConstants.API_ERRORS_UNAUTHORIZED));
    }

    /**
     * @return the notFoundException
     */
    public static NotFoundException getNotFoundException(final Object... args) {
        return new NotFoundException(getMessage(ErrorsConstants.API_ERRORS_NOTFOUND, args), buildKey(ErrorsConstants.API_ERRORS_NOTFOUND));
    }

    /**
     * @return the routeNotFoundException
     */
    public static RouteNotFoundException getRouteNotFoundException(final Object... args) {
        return new RouteNotFoundException(getMessage(ErrorsConstants.API_ERRORS_ROUTE_NOT_FOUND, args), buildKey(ErrorsConstants.API_ERRORS_ROUTE_NOT_FOUND));
    }

    /**
     * @return the validationException
     */
    public static ValidationException getValidationException(final Object... args) {
        return new ValidationException(getMessage(ErrorsConstants.API_ERRORS_VALIDATION, args), buildKey(ErrorsConstants.API_ERRORS_VALIDATION));
    }

    /**
     * @return the applicationServerException
     */
    public static ApplicationServerException getApplicationServerException(final Object... args) {
        return new ApplicationServerException(getMessage(ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR, args),
                buildKey(ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR));
    }

    /**
     * @return the parseOperationException
     */
    public static ParseOperationException getParseOperationException(final Object... args) {
        return new ParseOperationException(getMessage(ErrorsConstants.API_ERRORS_BAD_REQUEST, args), buildKey(ErrorsConstants.API_ERRORS_BAD_REQUEST));
    }

    /**
     * @return the internalServerException
     */
    public static InternalServerException getInternalServerException(final Object... args) {
        return new InternalServerException(getMessage(ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR, args),
                buildKey(ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR));
    }

    /**
     * @return the internalServerException
     */
    public static InternalServerException getInternalServerException(final Throwable e) {
        return new InternalServerException(getMessage(ErrorsConstants.API_ERRORS_INTERNAL_SERVER_ERROR), e);
    }

    /**
     * @return the notImplementedException
     */
    public static NotImplementedException getNotImplementedException(final Object... args) {
        return new NotImplementedException(getMessage(ErrorsConstants.API_ERRORS_ROUTE_NOT_IMPLEMENTED_YET, args),
                buildKey(ErrorsConstants.API_ERRORS_ROUTE_NOT_IMPLEMENTED_YET));
    }

    public static String buildKey(@SuppressWarnings("rawtypes") final Class clazz) {
        if (keysMappings.containsKey(clazz)) {
            return buildKey(keysMappings.get(clazz));
        }
        return null;
    }

    /**
     * Build key for error.
     * @param key
     * @return
     */
    private static String buildKey(final String key) {
        return ErrorsConstants.API_ERRORS + ErrorsConstants.MESSAGE_DOT + key;
    }

    /**
     * Get message for property name.
     * @param key
     * @param args
     * @return
     */
    private static String getMessage(final String key, final Object... args) {
        try {
            return String.format(apiErrorsMessages.get(key), args);
        }
        catch (final MissingFormatArgumentException exception) {
            throw getInternalServerException();
        }
    }

}
