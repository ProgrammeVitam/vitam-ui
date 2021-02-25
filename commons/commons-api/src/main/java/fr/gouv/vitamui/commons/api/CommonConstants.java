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
package fr.gouv.vitamui.commons.api;

/**
 * Global definition of constants like usual paths, values ...
 *
 *
 */
public class CommonConstants {

    public static final String ERROR_INVALID_PAGE_SIZE = "ERROR_INVALID_PAGE_SIZE";

    public static final String ERROR_INVALID_PAGE_NUMBER = "ERROR_INVALID_PAGE_NUMBER";

    public static final String ERROR_REQUIRED_VALUE_EMPTY_OR_NULL = "ERROR_REQUIRED_VALUE_EMPTY_OR_NULL";

    public static final String ADMIN_PATH = "/admin/v0";

    public static final String PATH_ID = "/{id}";

    public static final String PATH_LOGBOOK = "/{id}/history";

    public static final String PATH_DOC_TYPE = "/{type}";

    public static final String PATH_VITAM_ID = "/{vitamId}";

    public static final String PATH_CUSTOMER = "/{customerId}";

    public static final String PATH_ME = "/me";
    
    public static final String PATH_OBJECTS = "/objects";

    public static final String GDPR_STATUS = "/gdpr-status";

    public static final String PATH_ANALYTICS = "/analytics";

    public static final String X_TENANT_ID_HEADER = "X-Tenant-Id";

    public static final String X_XSRF_TOKEN_HEADER = "X-XSRF-Token";

    public static final String X_USER_TOKEN_HEADER = "X-User-Token";

    public static final String X_APPLICATION_ID_HEADER = "X-Application-Id";

    public static final String X_IDENTITY_HEADER = "X-Identity";

    public static final String X_REQUEST_ID_HEADER = "X-Request-Id";

    public static final String X_CHUNK_OFFSET_HEADER = "X-Chunk-Offset";

    public static final String X_TOTAL_SIZE_HEADER = "X-Total-Size";

    public static final String X_CUSTOMER_ID_HEADER = "X-Customer-Id";

    public static final String X_USER_LEVEL_HEADER = "X-User-Level";

    public static final String AJAX_HEADER_VALUE = "XMLHttpRequest";

    public static final String AJAX_HEADER_NAME = "X-Requested-With";

    public static final String X_LOGIN_REDIRECT_HEADER = "X-Login-Redirect";

    public static final String X_ACCESS_CONTRACT_ID_HEADER = "X-Access-Contract-Id";

    public static final String X_ORIGINAL_FILENAME_HEADER = "X-Original-Filename";

    public static final String X_OPERATION_ID_HEADER = "X-Operation-Id";

    public static final String ROLE_PREFIX = "ROLE_";

    public static final String GET_ROLE_PREFIX = ROLE_PREFIX + "GET_";

    public static final String GET_ME_ROLE_PREFIX = ROLE_PREFIX + "GET_ME_";

    public static final String CREATE_ROLE_PREFIX = ROLE_PREFIX + "CREATE_";

    public static final String UPDATE_ROLE_PREFIX = ROLE_PREFIX + "UPDATE_";

    public static final String UPDATE_ME_ROLE_PREFIX = ROLE_PREFIX + "UPDATE_ME_";

    public static final String DELETE_ROLE_PREFIX = ROLE_PREFIX + "DELETE_";

    public static final String CHECK_ROLE_PREFIX = ROLE_PREFIX + "CHECK_";

    public static final String DOWNLOAD_ROLE_PREFIX = ROLE_PREFIX + "DOWNLOAD_";

    public static final String UPLOAD_ROLE_PREFIX = ROLE_PREFIX + "UPLOAD_";

    public static final String EXPORT_ROLE_PREFIX = ROLE_PREFIX + "EXPORT_";

    public static final String IMPORT_ROLE_PREFIX = ROLE_PREFIX + "IMPORT_";

    public static final String PATH_CHECK = "/check";

    public static final String PATH_LEVELS = "/levels";

    public static final String PATH_IMPORT = "/import";

    public static final String PATH_EXPORT = "/export";

    public static final String USER_ID_ATTRIBUTE = "id";

    public static final String EMAIL_ATTRIBUTE = "email";

    public static final String FIRSTNAME_ATTRIBUTE = "firstname";

    public static final String LASTNAME_ATTRIBUTE = "lastname";

    public static final String IDENTIFIER_ATTRIBUTE = "identifier";

    public static final String OTP_ATTRIBUTE = "otp";

    public static final String SUBROGEABLE_ATTRIBUTE = "subrogeable";

    public static final String SUPER_USER_IDENTIFIER_ATTRIBUTE = "superUserIdentifier";

    public static final String LANGUAGE_ATTRIBUTE = "language";

    public static final String PHONE_ATTRIBUTE = "phone";

    public static final String ADDRESS_ATTRIBUTE = "address";

    public static final String MOBILE_ATTRIBUTE = "mobile";

    public static final String STATUS_ATTRIBUTE = "status";

    public static final String TYPE_ATTRIBUTE = "type";

    public static final String READONLY_ATTRIBUTE = "readonly";

    public static final String LEVEL_ATTRIBUTE = "level";

    public static final String LAST_CONNECTION_ATTRIBUTE = "lastConnection";

    public static final String NB_FAILED_ATTEMPTS_ATTRIBUTE = "nbFailedAttempts";

    public static final String PASSWORD_EXPIRATION_DATE_ATTRIBUTE = "passwordExpirationDate";

    public static final String GROUP_ID_ATTRIBUTE = "groupId";

    public static final String ANALYTICS_ATTRIBUTE = "analytics";

    public static final String PROFILE_GROUP_ATTRIBUTE = "profileGroup";

    public static final String ROLES_ATTRIBUTE = "roles";

    public static final String TENANTS_BY_APP_ATTRIBUTE = "tenantsByApp";

    public static final String SITE_CODE = "siteCode";

    public static final String CUSTOMER_ID_ATTRIBUTE = "customerId";

    public static final String CUSTOMER_IDENTIFIER_ATTRIBUTE = "customerIdentifier";

    public static final String BASIC_CUSTOMER_ATTRIBUTE = "basicCustomer";

    public static final String AUTHTOKEN_ATTRIBUTE = "authtoken";

    public static final String PROOF_TENANT_ID_ATTRIBUTE = "proofTenantId";

    public static final String PORTAL_URL = "PORTAL_URL";

    public static final String CAS_LOGIN_URL = "CAS_URL";

    public static final String CAS_LOGOUT_URL = "CAS_LOGOUT_URL";

    public static final String UI_URL = "UI_URL";

    public static final String ASSET_FOLDER = "ASSET_FOLDER";

    public static final String THEME_COLORS = "THEME_COLORS";

    public static final String PORTAL_MESSAGE = "PORTAL_MESSAGE";

    public static final String PORTAL_TITLE = "PORTAL_TITLE";

    public static final String CUSTOMER = "CUSTOMER";

    public static final String VERSION_RELEASE = "VERSION_RELEASE";


    /**
     * Constant contains application list for portal/header applications display
     */
    public static final String APPLICATION_CONFIGURATION = "APPLICATION_CONFIGURATION";

    /**
     * Constant contains category list for portal/header applications display
     */
    public static final String CATEGORY_CONFIGURATION = "CATEGORY_CONFIGURATION";

    /**
     * Constant contains redirect URL for cas after logout
     */
    public static final String LOGOUT_REDIRECT_UI_URL = "LOGOUT_REDIRECT_UI_URL";

    public static final String CAS_IDP_PARAMETER = "cas_idp";

    public static final String IDP_PARAMETER = "idp";

    public static final String CAS_USERNAME_PARAMETER = "cas_username";

    public static final String USERNAME_PARAMETER = "username";

    public static final String SUPER_USER_ATTRIBUTE = "_superUser";

    public static final String CUSTOMER_ID = "customerId";

    public static final String AUTH_TOKEN_PARAMETER = "authtoken";

    public static final String SURROGATION_PARAMETER = "surrogation";

    public static final String API_PARAMETER = "api";

    public static final String EMAIL_SEPARATOR = "@";

    public static final String CUSTOMERS_APPLICATIONS_NAME = "CUSTOMERS_APP";

    public static final String USERS_APPLICATIONS_NAME = "USERS_APP";

    public static final String PROFILES_GROUPS_APPLICATIONS_NAME = "GROUPS_APP";

    public static final String PROFILES_APPLICATIONS_NAME = "PROFILES_APP";

    public static final String SUBROGATION_APPLICATIONS_NAME = "SUBROGATIONS_APP";

    public static final String ACCOUNTS_APPLICATIONS_NAME = "ACCOUNTS_APP";

    public static final String HIERARCHY_PROFILE_APPLICATIONS_NAME = "HIERARCHY_PROFILE_APP";

    public static final String DEFAULT_INGEST_CONTRACT_IDENTIFIER = "IC-000001";

    public static final String DEFAULT_HOLDING_ACCESS_CONTRACT_IDENTIFIER = "AC-000001";

    public static final String DEFAULT_LOGBOOK_ACCESS_CONTRACT_IDENTIFIER = "AC-000002";

    public static final String API_VERSION_1 = "/v1";

    public static final String LOGBOOK_PATH = "/logbooks";

    public static final String LOGBOOK_OPERATIONS_PATH = LOGBOOK_PATH + "/operations";

    public static final String LOGBOOK_UNIT_LYFECYCLES_PATH = LOGBOOK_PATH + "/unitlifecycles" + PATH_ID;

    public static final String LOGBOOK_OBJECT_LYFECYCLES_PATH = LOGBOOK_PATH + "/objectslifecycles" + PATH_ID;

    public static final String LOGBOOK_OPERATION_BY_ID_PATH = LOGBOOK_OPERATIONS_PATH + PATH_ID;

    public static final String LOGBOOK_DOWNLOAD_PATH = LOGBOOK_OPERATIONS_PATH + PATH_ID + "/download";

    public static final String LOGBOOK_DOWNLOAD_ATR_PATH = LOGBOOK_DOWNLOAD_PATH + "/atr";

    public static final String LOGBOOK_DOWNLOAD_MANIFEST_PATH = LOGBOOK_DOWNLOAD_PATH + "/manifest";

    public static final String LOGBOOK_DOWNLOAD_REPORT_PATH = LOGBOOK_DOWNLOAD_PATH + "/{downloadType}";

    public static final String STATUS_API_DOCUMENTATION_TAGS = "status";

    public static final String STATUS_API_DOCUMENTATION_VALUE = "Status";

    public static final String STATUS_API_DOCUMENTATION_DESCRIPTION = "Status check and autotest";

    public static final Integer INPUT_STREAM_BUFFER_SIZE = 4096;

    public static final String GPDR_DEFAULT_VALUE = "-";

    public static final String APPLICATION_ID = "applicationId";

    /**
     * Constants of ingest operations
     */
    public static final String MULTIPART_FILE_PARAM_NAME = "uploadedFile";
    public static final String INGEST_UPLOAD = "/upload";
    public static final String X_ACTION = "X-Action";
    public static final String X_CONTEXT_ID = "X-Context-Id";
    public static final String X_SIZE_TOTAL = "X-Size-Total";
    public static final String X_CHUNK_OFFSET = "X-Chunk-Offset";
    public static final String LOGO = "LOGO";
}
