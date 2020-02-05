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

public final class ErrorsConstants {

    public static final String MESSAGE_DOT = ".";

    public static final String MESSAGE_PROPERTY_SEPARATOR = "_";

    public static final String ERRORS_DATE = "This date format '%s' was expected.";

    public static final String ERRORS_ENUM_STRING = "This field is mandatory. The expected values are '{' %s '}'.";

    public static final String ERRORS_NUMBER_FORMAT = "A numeric was expected.";

    public static final String ERRORS_NUMBER = "A numeric was expected.";

    public static final String ERRORS_STRICT_POSITIVE = "This value must be greater than zero.";

    public static final String ERRORS_STRING = "A string was expected.";

    public static final String ERRORS_VALID_ENUM = "This value '%s' is not permitted.";

    public static final String ERRORS_VALID_IDP_METADATA = "INVALID_FORMAT_IDP_METADATA";

    public static final String ERRORS_VALID_PRIVATE_KEYSPWD = "INVALID_PRIVATE_KEYSTORE_PASSWORD";

    public static final String ERRORS_VALID_KEYSPWD = "INVALID_KEYSTORE_PASSWORD";

    public static final String ERRORS_REQUIRED = "This field is mandatory.";

    public static final String ERRORS_EMAIL = "A valid email is required.";

    public static final String ERRORS_PATH_MISSING = "This field is mandatory";

    public static final String ERRORS_EMPTY = "This field can't be empty.";

    public static final String ERRORS_VALID_VALUE = "This value '%s' is not permitted.";

    public static final String API_ERRORS = "apierror";

    public static final String API_ERRORS_INTERNAL_SERVER_ERROR = "internalservererror";

    public static final String API_ERRORS_INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error during the process of the request.";

    public static final String API_ERRORS_BAD_REQUEST = "badrequest";

    public static final String API_ERRORS_BAD_REQUEST_MESSAGE = "Invalid Request : %s.";

    public static final String API_ERRORS_UNAUTHORIZED = "unauthorized";

    public static final String API_ERRORS_UNAUTHORIZED_MESSAGE = "Authentification is invalid.";

    public static final String API_ERRORS_NOTFOUND = "notfound";

    public static final String API_ERRORS_NOTFOUND_MESSAGE = "Resource not found.";

    public static final String API_ERRORS_VALIDATION = "validationerrors";

    public static final String API_ERRORS_VALIDATION_MESSAGE = "There were errors in the resquest.";

    public static final String API_ERRORS_NO_RIGHTS = "norights";

    public static final String API_ERRORS_NO_RIGHTS_MESSAGE = "You don't have the necessary authorizations to access this resource.";

    public static final String API_ERRORS_ROUTE_NOT_FOUND = "routenotfound";

    public static final String API_ERRORS_ROUTE_NOT_FOUND_MESSAGE = "The path '%s %s' was not found on this server.";

    public static final String API_ERRORS_BAD_FORMAT = "badformat";

    public static final String API_ERRORS_BAD_FORMAT_MESSAGE = "The request doesn't respect the JSON format.";

    public static final String API_ERRORS_ROUTE_NOT_IMPLEMENTED_YET = "notimplemented";

    public static final String API_ERRORS_ROUTE_NOT_IMPLEMENTED_YET_MESSAGE = "Resource not yet implemented.";

    public static final String ERROR_TIMESTAMP = "timestamp";

    public static final String ERROR_STATUS = "status";

    public static final String ERROR_KEY = "error";

    public static final String ERROR_EXCEPTION = "exception";

    public static final String ERROR_MESSAGE = "message";

    public static final String ERROR_ARGS = "args";

    private ErrorsConstants() {
        // do nothing
    }

}
