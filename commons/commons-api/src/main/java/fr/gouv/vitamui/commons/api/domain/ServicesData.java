/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 * <p>
 * contact@programmevitam.fr
 * <p>
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 * <p>
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
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
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.api.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.commons.api.CommonConstants.ABORT_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.CHECK_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.CLOSE_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.CREATE_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.DELETE_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.EXPORT_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.GET_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.IMPORT_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.REOPEN_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.SEND_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.UPDATE_ME_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.UPDATE_ROLE_PREFIX;

/**
 * All the services.
 */
public class ServicesData {

    protected ServicesData() {}

    //------------------------------------ USERS -------------------------------------------
    public static final String SERVICE_USERS = "USERS";

    public static final String SERVICE_USERS_PROFILES_NAMES = "Profil pour la gestion des utilisateurs";

    public static final String ROLE_GET_USERS = GET_ROLE_PREFIX + SERVICE_USERS;

    public static final String ROLE_GET_USERS_ALL_CUSTOMERS = ROLE_GET_USERS + "_ALL_CUSTOMERS";

    public static final String ROLE_CREATE_USERS = CREATE_ROLE_PREFIX + SERVICE_USERS;

    public static final String ROLE_UPDATE_USERS = UPDATE_ROLE_PREFIX + SERVICE_USERS;

    public static final String ROLE_UPDATE_STANDARD_USERS = UPDATE_ROLE_PREFIX + "STANDARD_" + SERVICE_USERS;

    public static final String ROLE_GENERIC_USERS = "ROLE_GENERIC_" + SERVICE_USERS;

    public static final String ROLE_ANONYMIZATION_USERS = "ROLE_ANONYMIZATION_" + SERVICE_USERS;

    public static final String ROLE_MFA_USERS = "ROLE_MFA_" + SERVICE_USERS;

    public static final String ROLE_UPDATE_ME_USERS = UPDATE_ME_ROLE_PREFIX + SERVICE_USERS;

    //------------------------------------ USERS INFO ROLE_GET_USER_INFOS  -------------------------------------------

    public static final String SERVICE_USER_INFOS = "USER_INFOS";
    public static final String ROLE_GET_USER_INFOS = GET_ROLE_PREFIX + SERVICE_USER_INFOS;
    public static final String ROLE_CREATE_USER_INFOS = CREATE_ROLE_PREFIX + SERVICE_USER_INFOS;
    public static final String ROLE_UPDATE_USER_INFOS = UPDATE_ROLE_PREFIX + SERVICE_USER_INFOS;

    //------------------------------------ CUSTOMERS -------------------------------------------

    public static final String SERVICE_CUSTOMERS = "CUSTOMERS";

    public static final String ROLE_GET_CUSTOMERS = GET_ROLE_PREFIX + SERVICE_CUSTOMERS;

    public static final String ROLE_CREATE_CUSTOMERS = CREATE_ROLE_PREFIX + SERVICE_CUSTOMERS;

    public static final String ROLE_UPDATE_CUSTOMERS = UPDATE_ROLE_PREFIX + SERVICE_CUSTOMERS;

    //------------------------------------ OWNERS -------------------------------------------

    public static final String SERVICE_OWNERS = "OWNERS";

    public static final String ROLE_GET_OWNERS = GET_ROLE_PREFIX + SERVICE_OWNERS;

    public static final String ROLE_CREATE_OWNERS = CREATE_ROLE_PREFIX + SERVICE_OWNERS;

    public static final String ROLE_UPDATE_OWNERS = UPDATE_ROLE_PREFIX + SERVICE_OWNERS;

    //------------------------------------ TENANTS -------------------------------------------

    public static final String SERVICE_TENANTS = "TENANTS";

    /**
     * Get authorized tenants
     */
    public static final String ROLE_GET_TENANTS = GET_ROLE_PREFIX + SERVICE_TENANTS;

    /**
     * GET all instance tenants for superadmin
     */
    public static final String ROLE_GET_ALL_TENANTS = GET_ROLE_PREFIX + "ALL_" + SERVICE_TENANTS;

    /**
     * Get all customer tenants. A restriction key for each customer will be added to the request.
     * Users can retrieve all customer tenants even if they do not have access to.
     */
    public static final String ROLE_GET_TENANTS_MY_CUSTOMER = GET_ROLE_PREFIX + SERVICE_TENANTS + "_MY_CUSTOMER";

    public static final String ROLE_CREATE_TENANTS = CREATE_ROLE_PREFIX + SERVICE_TENANTS;

    public static final String ROLE_CREATE_TENANTS_ALL_CUSTOMERS = ROLE_CREATE_TENANTS + "_ALL_CUSTOMERS";

    public static final String ROLE_UPDATE_TENANTS = UPDATE_ROLE_PREFIX + SERVICE_TENANTS;

    public static final String ROLE_UPDATE_TENANTS_ALL_CUSTOMERS = ROLE_UPDATE_TENANTS + "_ALL_CUSTOMERS";

    //------------------------------------ PROVIDERS -------------------------------------------

    public static final String SERVICE_PROVIDERS = "PROVIDERS";

    public static final String ROLE_GET_PROVIDERS = GET_ROLE_PREFIX + SERVICE_PROVIDERS;

    public static final String ROLE_CREATE_PROVIDERS = CREATE_ROLE_PREFIX + SERVICE_PROVIDERS;

    public static final String ROLE_UPDATE_PROVIDERS = UPDATE_ROLE_PREFIX + SERVICE_PROVIDERS;

    public static final String ROLE_DELETE_PROVIDERS = DELETE_ROLE_PREFIX + SERVICE_PROVIDERS;

    //------------------------------------  GROUPS -------------------------------------------

    public static final String SERVICE_GROUPS = "GROUPS";

    public static final String SERVICE_GROUPS_PROFILES_NAMES = "Profil pour la gestion des groupes d'utilisateurs";

    public static final String ROLE_GET_GROUPS = GET_ROLE_PREFIX + SERVICE_GROUPS;

    public static final String ROLE_GET_ALL_GROUPS = GET_ROLE_PREFIX + "ALL_" + SERVICE_GROUPS;

    public static final String ROLE_CREATE_GROUPS = CREATE_ROLE_PREFIX + SERVICE_GROUPS;

    public static final String ROLE_UPDATE_GROUPS = UPDATE_ROLE_PREFIX + SERVICE_GROUPS;

    public static final String ROLE_DELETE_GROUPS = DELETE_ROLE_PREFIX + SERVICE_GROUPS;

    //------------------------------------ PROFILES -------------------------------------------

    public static final String SERVICE_PROFILES = "PROFILES";

    public static final String SERVICE_PROFILES_PROFILES_NAMES = "Profil pour la gestion des profils d'utilisateurs";

    public static final String ROLE_GET_PROFILES = GET_ROLE_PREFIX + SERVICE_PROFILES;

    public static final String ROLE_CREATE_PROFILES = CREATE_ROLE_PREFIX + SERVICE_PROFILES;

    public static final String ROLE_UPDATE_PROFILES = UPDATE_ROLE_PREFIX + SERVICE_PROFILES;

    public static final String ROLE_DELETE_PROFILES = DELETE_ROLE_PREFIX + SERVICE_PROFILES;

    public static final String ROLE_GET_PROFILES_ALL_TENANTS =
        GET_ROLE_PREFIX + SERVICE_PROFILES + "_ALL_" + SERVICE_TENANTS;

    //------------------------------------ SUBROGATIONS -------------------------------------------

    public static final String SERVICE_SUBROGATIONS = "SUBROGATIONS";

    public static final String ROLE_GET_SUBROGATIONS = GET_ROLE_PREFIX + SERVICE_SUBROGATIONS;

    public static final String ROLE_GET_USERS_SUBROGATIONS = GET_ROLE_PREFIX + "USERS_" + SERVICE_SUBROGATIONS;

    public static final String ROLE_GET_GROUPS_SUBROGATIONS = GET_ROLE_PREFIX + "GROUPS_" + SERVICE_SUBROGATIONS;

    public static final String ROLE_CREATE_SUBROGATIONS = CREATE_ROLE_PREFIX + SERVICE_SUBROGATIONS;

    public static final String ROLE_DELETE_SUBROGATIONS = DELETE_ROLE_PREFIX + SERVICE_SUBROGATIONS;

    //------------------------------------ CAS -------------------------------------------

    public static final String ROLE_CAS_LOGIN = ROLE_PREFIX + "CAS_LOGIN";

    public static final String ROLE_CAS_LOGOUT = ROLE_PREFIX + "CAS_LOGOUT";

    public static final String ROLE_CAS_CHANGE_PASSWORD = ROLE_PREFIX + "CAS_CHANGE_PASSWORD";

    public static final String ROLE_CAS_USERS = ROLE_PREFIX + "CAS_USERS";

    public static final String ROLE_CAS_CUSTOMER_IDS = ROLE_PREFIX + "CAS_CUSTOMERS";

    public static final String ROLE_CAS_SUBROGATIONS = ROLE_PREFIX + "CAS_SUBROGATIONS";

    public static final String ROLE_LOGBOOKS = "ROLE_LOGBOOKS";

    //------------------------------------ TECHNICAL CHECKS -------------------------------------------

    public static final String ROLE_CHECK_TENANTS = CHECK_ROLE_PREFIX + SERVICE_TENANTS;

    public static final String ROLE_CHECK_USERS = CHECK_ROLE_PREFIX + SERVICE_USERS;

    //------------------------------------ ACCOUNTS -------------------------------------------

    public static final String SERVICE_ACCOUNTS = "ACCOUNTS";

    public static final String SERVICE_ACCOUNTS_PROFILES_NAMES = "Profil pour la gestion de mon compte";

    //------------------------------------ APPLICATIONS -------------------------------------------

    public static final String SERVICE_APPLICATIONS = "APPLICATIONS";

    //------------------------------------ ARCHIVE PROFILES -------------------------------------------

    public static final String SERVICE_ARCHIVES_PROFILES = "ARCHIVE_PROFILE";

    public static final String ROLE_GET_ALL_ACCESS_CONTRACTS = GET_ROLE_PREFIX + "ALL_ACCESS_CONTRACTS";

    public static final String ROLE_GET_TENANT_HOLDING = GET_ROLE_PREFIX + "TENANT_HOLDING";

    //------------------------------------ HIERARCHY PROFILES -----------------------------------------

    public static final String SERVICE_HIERARCHY_PROFILES = "HIERARCHY_PROFILES";

    // tests
    public static final String APP_SAE = "sae";

    public static final String APP_GED = "ged";

    //------------------------------------ ACCESS CONTRACT -----------------------------------------

    public static final String SERVICE_ACCESS_CONTRACT = "ACCESS_CONTRACTS";

    public static final String ROLE_CREATE_ACCESS_CONTRACTS = CREATE_ROLE_PREFIX + SERVICE_ACCESS_CONTRACT;

    public static final String ROLE_GET_ACCESS_CONTRACTS = GET_ROLE_PREFIX + SERVICE_ACCESS_CONTRACT;

    public static final String ROLE_UPDATE_ACCESS_CONTRACTS = UPDATE_ROLE_PREFIX + SERVICE_ACCESS_CONTRACT;

    //------------------------------------ PASTIS -----------------------------------------

    public static final String SERVICE_PASTIS = "PASTIS";

    public static final String ROLE_GET_PASTIS = GET_ROLE_PREFIX + SERVICE_PASTIS;

    public static final String ROLE_CREATE_PASTIS = CREATE_ROLE_PREFIX + SERVICE_PASTIS;

    public static final String ROLE_UPDATE_PASTIS = UPDATE_ROLE_PREFIX + SERVICE_PASTIS;

    public static final String ROLE_DELETE_PASTIS = DELETE_ROLE_PREFIX + SERVICE_PASTIS;

    //------------------------------------ INGEST CONTRACT -----------------------------------------

    public static final String SERVICE_INGEST_CONTRACT = "INGEST_CONTRACTS";

    public static final String SERVICE_FILLING_PLAN_ACCESS = "FILLING_PLAN_ACCESS";

    public static final String ROLE_CREATE_INGEST_CONTRACTS = CREATE_ROLE_PREFIX + SERVICE_INGEST_CONTRACT;

    public static final String ROLE_GET_INGEST_CONTRACTS = GET_ROLE_PREFIX + SERVICE_INGEST_CONTRACT;

    public static final String ROLE_UPDATE_INGEST_CONTRACTS = UPDATE_ROLE_PREFIX + SERVICE_INGEST_CONTRACT;

    public static final String ROLE_GET_FILLING_PLAN_ACCESS = GET_ROLE_PREFIX + SERVICE_FILLING_PLAN_ACCESS;

    //------------------------------------ AGENCIES -----------------------------------------

    public static final String SERVICE_AGENCIES = "AGENCIES";

    public static final String ROLE_GET_AGENCIES = GET_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_CREATE_AGENCIES = CREATE_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_UPDATE_AGENCIES = UPDATE_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_DELETE_AGENCIES = DELETE_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_EXPORT_AGENCIES = EXPORT_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_IMPORT_AGENCIES = IMPORT_ROLE_PREFIX + SERVICE_AGENCIES;

    //---------------------------------- FILE FORMATS --------------------------------------------

    public static final String SERVICE_FILE_FORMATS = "FILE_FORMATS";

    public static final String ROLE_GET_FILE_FORMATS = GET_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    public static final String ROLE_CREATE_FILE_FORMATS = CREATE_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    public static final String ROLE_UPDATE_FILE_FORMATS = UPDATE_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    public static final String ROLE_DELETE_FILE_FORMATS = DELETE_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    public static final String ROLE_EXPORT_FILE_FORMATS = EXPORT_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    public static final String ROLE_IMPORT_FILE_FORMATS = IMPORT_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    //------------------------------------ CONTEXTS -----------------------------------------

    public static final String SERVICE_CONTEXTS = "CONTEXTS";

    public static final String ROLE_GET_CONTEXTS = GET_ROLE_PREFIX + SERVICE_CONTEXTS;

    public static final String ROLE_CREATE_CONTEXTS = CREATE_ROLE_PREFIX + SERVICE_CONTEXTS;

    public static final String ROLE_UPDATE_CONTEXTS = UPDATE_ROLE_PREFIX + SERVICE_CONTEXTS;

    //-------------------------------- SECURITY PROFILE ---------------------------------------

    public static final String SERVICE_SECURITY_PROFILES = "SECURITY_PROFILES";

    public static final String ROLE_GET_SECURITY_PROFILES = GET_ROLE_PREFIX + SERVICE_SECURITY_PROFILES;

    public static final String ROLE_CREATE_SECURITY_PROFILES = CREATE_ROLE_PREFIX + SERVICE_SECURITY_PROFILES;

    public static final String ROLE_UPDATE_SECURITY_PROFILES = UPDATE_ROLE_PREFIX + SERVICE_SECURITY_PROFILES;

    public static final String ROLE_DELETE_SECURITY_PROFILES = DELETE_ROLE_PREFIX + SERVICE_SECURITY_PROFILES;

    //--------------------------------- REGLES DE GESTION -------------------------------------

    public static final String SERVICE_RULES = "RULES";

    public static final String ROLE_GET_RULES = GET_ROLE_PREFIX + SERVICE_RULES;

    public static final String ROLE_CREATE_RULES = CREATE_ROLE_PREFIX + SERVICE_RULES;

    public static final String ROLE_UPDATE_RULES = CREATE_ROLE_PREFIX + SERVICE_RULES;

    public static final String ROLE_DELETE_RULES = DELETE_ROLE_PREFIX + SERVICE_RULES;

    public static final String ROLE_IMPORT_RULES = IMPORT_ROLE_PREFIX + SERVICE_RULES;

    //------------------------------------ ONTOLOGIES -----------------------------------------

    public static final String SERVICE_ONTOLOGIES = "ONTOLOGIES";

    public static final String ROLE_GET_ONTOLOGIES = GET_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    public static final String ROLE_CREATE_ONTOLOGIES = CREATE_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    public static final String ROLE_UPDATE_ONTOLOGIES = UPDATE_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    public static final String ROLE_DELETE_ONTOLOGIES = DELETE_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    public static final String ROLE_IMPORT_ONTOLOGIES = IMPORT_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    //------------------------------------ ONTOLOGIES -----------------------------------------

    public static final String SERVICE_SCHEMAS = "SCHEMAS";

    public static final String ROLE_GET_SCHEMAS = GET_ROLE_PREFIX + SERVICE_SCHEMAS;

    //------------------------------------- OPERATIONS --------------------------------------------

    public static final String SERVICE_OPERATIONS = "OPERATIONS";

    public static final String ROLE_GET_OPERATIONS = GET_ROLE_PREFIX + SERVICE_OPERATIONS;

    public static final String ROLE_GET_FILE_OPERATION = GET_ROLE_PREFIX + "FILE_" + SERVICE_OPERATIONS;

    //------------------------------------- AUDITS --------------------------------------------

    public static final String SERVICE_AUDITS = "AUDITS";

    public static final String ROLE_GET_AUDITS = ROLE_PREFIX + "GET_" + SERVICE_AUDITS;

    public static final String ROLE_RUN_AUDITS = ROLE_PREFIX + "RUN_" + SERVICE_AUDITS;

    //------------------------------------- PROBATIVE_VALUE --------------------------------------------

    public static final String SERVICE_PROBATIVE_VALUE = "PROBATIVE_VALUE";

    public static final String ROLE_RUN_PROBATIVE_VALUE = "ROLE_RUN_" + SERVICE_PROBATIVE_VALUE;

    //------------------------------ PROFILES (ARCHIVE PROFILES) --------------------------------------

    public static final String SERVICE_ARCHIVE_PROFILES = "ARCHIVE_PROFILES";

    public static final String ROLE_GET_ARCHIVE_PROFILES = GET_ROLE_PREFIX + SERVICE_ARCHIVE_PROFILES;

    public static final String ROLE_UPDATE_ARCHIVE_PROFILES = CREATE_ROLE_PREFIX + SERVICE_ARCHIVE_PROFILES;

    public static final String ROLE_CREATE_ARCHIVE_PROFILES = UPDATE_ROLE_PREFIX + SERVICE_ARCHIVE_PROFILES;

    public static final String ROLE_IMPORT_ARCHIVE_PROFILES = DELETE_ROLE_PREFIX + SERVICE_ARCHIVE_PROFILES;

    //------------------------------------ ARCHIVE PROFILES UNIT -----------------------------------

    public static final String SERVICE_ARCHIVE_PROFILES_UNIT = "ARCHIVE_PROFILES_UNIT";

    public static final String ROLE_GET_ARCHIVE_PROFILES_UNIT = GET_ROLE_PREFIX + SERVICE_ARCHIVE_PROFILES_UNIT;

    public static final String ROLE_UPDATE_ARCHIVE_PROFILES_UNIT = CREATE_ROLE_PREFIX + SERVICE_ARCHIVE_PROFILES_UNIT;

    public static final String ROLE_CREATE_ARCHIVE_PROFILES_UNIT = UPDATE_ROLE_PREFIX + SERVICE_ARCHIVE_PROFILES_UNIT;

    public static final String ROLE_IMPORT_ARCHIVE_PROFILES_UNIT = DELETE_ROLE_PREFIX + SERVICE_ARCHIVE_PROFILES_UNIT;

    //------------------------------------ INGESTS -----------------------------------------

    public static final String SERVICE_INGEST = "INGEST";
    public static final String ROLE_CREATE_INGEST = "ROLE_CREATE_INGEST";
    public static final String ROLE_GET_INGEST = "ROLE_GET_INGEST";
    public static final String ROLE_GET_ALL_INGEST = "ROLE_GET_ALL_INGEST";

    //------------------------------------ ARCHIVE SEARCH Roles -----------------------------------------

    public static final String SERVICE_ARCHIVE = "ARCHIVE_SEARCH";

    public static final String ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT_ROLE = "ROLE_ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT";
    public static final String ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE = "ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH";
    public static final String ARCHIVE_SEARCH_ROLE_GET_ARCHIVE_BINARY = "ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_BINARY";
    public static final String ROLE_EXPORT_DIP = ROLE_PREFIX + "EXPORT_DIP";
    public static final String ROLE_TRANSFER_REQUEST = ROLE_PREFIX + "TRANSFER_REQUEST";
    public static final String ROLE_ELIMINATION = ROLE_PREFIX + "ELIMINATION";
    public static final String ROLE_COMPUTED_INHERITED_RULES = ROLE_PREFIX + "COMPUTED_INHERITED_RULES";
    public static final String ROLE_RECLASSIFICATION = ROLE_PREFIX + "RECLASSIFICATION";
    public static final String ROLE_TRANSFER_ACKNOWLEDGMENT = ROLE_PREFIX + "TRANSFER_ACKNOWLEDGMENT";

    //------------------------------------ Collect  Roles -----------------------------------------

    public static final String COLLECT_UPDATE_BULK_ARCHIVE_UNIT_ROLE = "ROLE_COLLECT_UPDATE_BULK_ARCHIVE_UNIT";
    public static final String COLLECT_UPDATE_UNITARY_ARCHIVE_UNIT_ROLE = "ROLE_COLLECT_UPDATE_UNITARY_ARCHIVE_UNIT";

    public static final String COLLECT_GET_ARCHIVE_SEARCH_ROLE = "ROLE_COLLECT_GET_ARCHIVE_SEARCH";

    public static final String COLLECT_ROLE_GET_ARCHIVE_BINARY = "ROLE_COLLECT_GET_ARCHIVE_BINARY";

    //------------------------------------ API TREES & PLANS -----------------------------------------

    public static final String SERVICE_HOLDING_FILLING_SCHEME_ROLE = "HOLDING_FILLING_SCHEME";

    public static final String ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE =
        CREATE_ROLE_PREFIX + SERVICE_HOLDING_FILLING_SCHEME_ROLE;
    public static final String ROLE_GET_HOLDING_FILLING_SCHEME_ROLE =
        GET_ROLE_PREFIX + SERVICE_HOLDING_FILLING_SCHEME_ROLE;
    public static final String ROLE_GET_ALL_HOLDING_FILLING_SCHEME_ROLE =
        GET_ROLE_PREFIX + "ALL_" + SERVICE_HOLDING_FILLING_SCHEME_ROLE;

    //------------------------------------ UNITS -----------------------------------------

    public static final String SERVICE_UNITS = "UNITS";

    public static final String ROLE_GET_UNITS = GET_ROLE_PREFIX + SERVICE_UNITS;

    //------------------------------------ EXTERNAL PARAMETERS -------------------------------------------
    public static final String SERVICE_EXTERNAL_PARAMS = "EXTERNAL_PARAMS";

    public static final String ROLE_GET_EXTERNAL_PARAMS = GET_ROLE_PREFIX + SERVICE_EXTERNAL_PARAMS;

    //------------------------------------ LOGBOOK MANAGEMENT OPERATION -----------------------------------------

    public static final String SERVICE_LOGBOOK_OPERATION = "LOGBOOK_OPERATION";

    public static final String ROLE_UPDATE_LOGBOOK_OPERATION = UPDATE_ROLE_PREFIX + SERVICE_LOGBOOK_OPERATION;
    public static final String ROLE_GET_LOGBOOK_OPERATION = GET_ROLE_PREFIX + SERVICE_LOGBOOK_OPERATION;
    public static final String ROLE_GET_ALL_LOGBOOK_OPERATION = GET_ROLE_PREFIX + "ALL_" + SERVICE_LOGBOOK_OPERATION;

    //------------------------------------ ACCESS CONTRACT EXTERNAL PARAMETERS -------------------------------------------
    public static final String ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE = "EXTERNAL_PARAM_PROFILE";

    public static final String ROLE_GET_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE =
        CREATE_ROLE_PREFIX + ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE;
    public static final String ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE =
        "ROLE_EDIT_" + ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE;
    public static final String ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE =
        "ROLE_SEARCH_" + ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE;

    public static final String ACCESSION_REGISTER_DETAIL = "ACCESSION_REGISTER_DETAIL";

    public static final String ROLE_GET_ACCESSION_REGISTER_DETAIL = GET_ROLE_PREFIX + ACCESSION_REGISTER_DETAIL;

    //------------------------------------ COLLECT -----------------------------------------
    public static final String PROJECTS = "PROJECTS";

    public static final String TRANSACTIONS = "TRANSACTIONS";
    public static final String UNITS_METADATA = "UNITS_METADATA";
    public static final String ROLE_GET_PROJECTS = GET_ROLE_PREFIX + PROJECTS;
    public static final String ROLE_CREATE_PROJECTS = CREATE_ROLE_PREFIX + PROJECTS;
    public static final String ROLE_CREATE_TRANSACTIONS = CREATE_ROLE_PREFIX + TRANSACTIONS;
    public static final String ROLE_UPDATE_PROJECTS = UPDATE_ROLE_PREFIX + PROJECTS;
    public static final String ROLE_UPDATE_TRANSACTIONS = UPDATE_ROLE_PREFIX + TRANSACTIONS;

    public static final String ROLE_CLOSE_TRANSACTIONS = CLOSE_ROLE_PREFIX + TRANSACTIONS;

    public static final String ROLE_SEND_TRANSACTIONS = SEND_ROLE_PREFIX + TRANSACTIONS;
    public static final String ROLE_GET_TRANSACTIONS = GET_ROLE_PREFIX + TRANSACTIONS;
    public static final String ROLE_DELETE_PROJECTS = GET_ROLE_PREFIX + PROJECTS;

    public static final String ROLE_REOPEN_TRANSACTIONS = REOPEN_ROLE_PREFIX + TRANSACTIONS;

    public static final String ROLE_ABORT_TRANSACTIONS = ABORT_ROLE_PREFIX + TRANSACTIONS;

    public static final String ROLE_UPDATE_UNITS_METADATA = UPDATE_ROLE_PREFIX + UNITS_METADATA;

    //------------------------------------ MANAGEMENT CONTRACT -------------------------------------------
    public static final String SERVICE_MANAGEMENT_CONTRACT = "MANAGEMENT_CONTRACT";

    public static final String ROLE_GET_MANAGEMENT_CONTRACT = GET_ROLE_PREFIX + SERVICE_MANAGEMENT_CONTRACT;

    public static final String ROLE_CREATE_MANAGEMENT_CONTRACT = CREATE_ROLE_PREFIX + SERVICE_MANAGEMENT_CONTRACT;

    public static final String ROLE_DELETE_MANAGEMENT_CONTRACT = DELETE_ROLE_PREFIX + SERVICE_MANAGEMENT_CONTRACT;

    public static final String ROLE_UPDATE_MANAGEMENT_CONTRACT = UPDATE_ROLE_PREFIX + SERVICE_MANAGEMENT_CONTRACT;

    //@formatter:off

    //------------------------------ PROVISIONING USERS ------------------------------------------------

    public static final String ROLE_PROVISIONING_USER = ROLE_PREFIX + "PROVISIONING_USER";

    /**
     * List of the admin roles for the VITAMUI application.
     */

    private static final List<String> ADMIN_VITAMUI_ROLES = Arrays.asList(
        ROLE_GET_CUSTOMERS,
        ROLE_CREATE_CUSTOMERS,
        ROLE_UPDATE_CUSTOMERS,
        ROLE_GET_USERS_ALL_CUSTOMERS,

        ROLE_CREATE_TENANTS_ALL_CUSTOMERS,
        ROLE_GET_ALL_TENANTS,
        ROLE_UPDATE_TENANTS_ALL_CUSTOMERS,

        ROLE_GET_PROVIDERS,
        ROLE_CREATE_PROVIDERS,
        ROLE_UPDATE_PROVIDERS,
        ROLE_DELETE_PROVIDERS,


        ROLE_GET_PROFILES_ALL_TENANTS,

        ROLE_GET_OWNERS,
        ROLE_CREATE_OWNERS,
        ROLE_UPDATE_OWNERS,

        ROLE_GET_SUBROGATIONS,
        ROLE_CREATE_SUBROGATIONS,
        ROLE_DELETE_SUBROGATIONS,
        ROLE_GET_USERS_SUBROGATIONS,
        ROLE_GET_GROUPS_SUBROGATIONS,

        ROLE_CAS_LOGIN,
        ROLE_CAS_LOGOUT,
        ROLE_CAS_CHANGE_PASSWORD,
        ROLE_CAS_USERS,
        ROLE_CAS_SUBROGATIONS,

        ROLE_CHECK_USERS,
        ROLE_CHECK_TENANTS,

        ROLE_CREATE_ACCESS_CONTRACTS,
        ROLE_GET_ACCESS_CONTRACTS,
        ROLE_UPDATE_ACCESS_CONTRACTS,

        ROLE_CREATE_INGEST_CONTRACTS,
        ROLE_GET_INGEST_CONTRACTS,
        ROLE_UPDATE_INGEST_CONTRACTS,
        ROLE_GET_FILLING_PLAN_ACCESS,

        ROLE_GET_AGENCIES,
        ROLE_UPDATE_AGENCIES,
        ROLE_CREATE_AGENCIES,
        ROLE_DELETE_AGENCIES,
        ROLE_EXPORT_AGENCIES,

        ROLE_GET_FILE_FORMATS,
        ROLE_DELETE_FILE_FORMATS,
        ROLE_CREATE_FILE_FORMATS,
        ROLE_UPDATE_FILE_FORMATS,

        ROLE_GET_CONTEXTS,
        ROLE_CREATE_CONTEXTS,
        ROLE_UPDATE_CONTEXTS,

        ROLE_GET_SECURITY_PROFILES,
        ROLE_UPDATE_SECURITY_PROFILES,
        ROLE_CREATE_SECURITY_PROFILES,
        ROLE_DELETE_SECURITY_PROFILES,

        ROLE_GET_RULES,
        ROLE_CREATE_RULES,
        ROLE_UPDATE_RULES,
        ROLE_DELETE_RULES,

        ROLE_GET_ONTOLOGIES,
        ROLE_CREATE_ONTOLOGIES,
        ROLE_UPDATE_ONTOLOGIES,
        ROLE_DELETE_ONTOLOGIES,

        ROLE_GET_AUDITS,
        ROLE_RUN_AUDITS,
        ROLE_GET_OPERATIONS,
        ROLE_RUN_PROBATIVE_VALUE,

        ROLE_GET_ALL_INGEST,
        ROLE_GET_INGEST,
        ROLE_CREATE_INGEST,

        ROLE_LOGBOOKS,

        ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE,
        ROLE_EXPORT_DIP,
        ROLE_TRANSFER_REQUEST,
        ROLE_ELIMINATION,
        ROLE_COMPUTED_INHERITED_RULES,
        ROLE_TRANSFER_ACKNOWLEDGMENT,

        ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE,
        ROLE_GET_HOLDING_FILLING_SCHEME_ROLE,
        ROLE_GET_ALL_HOLDING_FILLING_SCHEME_ROLE,

        ROLE_GET_UNITS,

        ROLE_GET_EXTERNAL_PARAMS,

        ROLE_UPDATE_LOGBOOK_OPERATION,
        ROLE_GET_LOGBOOK_OPERATION,
        ROLE_GET_ALL_LOGBOOK_OPERATION,

        ROLE_GET_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
        ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
        ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
        ROLE_GET_ALL_LOGBOOK_OPERATION,

        ROLE_GET_ACCESSION_REGISTER_DETAIL,
        ROLE_RECLASSIFICATION,

        ROLE_GET_ACCESSION_REGISTER_DETAIL,

        ROLE_GET_MANAGEMENT_CONTRACT,
        ROLE_CREATE_MANAGEMENT_CONTRACT,
        ROLE_DELETE_MANAGEMENT_CONTRACT,
        ROLE_UPDATE_MANAGEMENT_CONTRACT,

        ROLE_GET_ACCESSION_REGISTER_DETAIL,

        ROLE_RECLASSIFICATION,


        ROLE_GET_PROJECTS,
        ROLE_CREATE_PROJECTS,
        ROLE_UPDATE_PROJECTS,

        ROLE_CLOSE_TRANSACTIONS,
        ROLE_SEND_TRANSACTIONS,
        ROLE_CREATE_TRANSACTIONS,
        ROLE_UPDATE_TRANSACTIONS,
        ROLE_GET_TRANSACTIONS,
        ROLE_DELETE_PROJECTS,
        ROLE_SEND_TRANSACTIONS,
        COLLECT_UPDATE_BULK_ARCHIVE_UNIT_ROLE,
        COLLECT_UPDATE_UNITARY_ARCHIVE_UNIT_ROLE
    );

    /**
     * List of all the roles in the VITAMUI application (including the admin roles present in the ADMIN_VITAMUI_ROLES list)
     */

    private static final List<String> ROLE_NAMES = Arrays.asList(
        ROLE_GET_USERS, ROLE_CREATE_USERS,
        ROLE_GET_USERS_ALL_CUSTOMERS,
        ROLE_UPDATE_USERS,
        ROLE_UPDATE_STANDARD_USERS,
        ROLE_GENERIC_USERS,
        ROLE_MFA_USERS,
        ROLE_ANONYMIZATION_USERS,
        ROLE_UPDATE_ME_USERS,

        ROLE_GET_USER_INFOS,
        ROLE_CREATE_USER_INFOS,
        ROLE_UPDATE_USER_INFOS,


        ROLE_GET_CUSTOMERS,
        ROLE_CREATE_CUSTOMERS,
        ROLE_UPDATE_CUSTOMERS,

        ROLE_GET_TENANTS,
        ROLE_CREATE_TENANTS,
        ROLE_CREATE_TENANTS_ALL_CUSTOMERS,
        ROLE_UPDATE_TENANTS,
        ROLE_GET_ALL_TENANTS,
        ROLE_GET_TENANTS_MY_CUSTOMER,
        ROLE_UPDATE_TENANTS_ALL_CUSTOMERS,

        ROLE_GET_PROVIDERS,
        ROLE_CREATE_PROVIDERS,
        ROLE_UPDATE_PROVIDERS,
        ROLE_DELETE_PROVIDERS,

        ROLE_GET_GROUPS,
        ROLE_GET_ALL_GROUPS,
        ROLE_CREATE_GROUPS,
        ROLE_UPDATE_GROUPS,
        ROLE_DELETE_GROUPS,

        ROLE_GET_PROFILES,
        ROLE_CREATE_PROFILES,
        ROLE_UPDATE_PROFILES,
        ROLE_DELETE_PROFILES,
        ROLE_GET_PROFILES_ALL_TENANTS,

        ROLE_GET_OWNERS,
        ROLE_CREATE_OWNERS,
        ROLE_UPDATE_OWNERS,

        ROLE_GET_SUBROGATIONS,
        ROLE_CREATE_SUBROGATIONS,
        ROLE_DELETE_SUBROGATIONS,
        ROLE_GET_USERS_SUBROGATIONS,
        ROLE_GET_GROUPS_SUBROGATIONS,

        ROLE_CAS_LOGIN,
        ROLE_CAS_LOGOUT,
        ROLE_CAS_CHANGE_PASSWORD,
        ROLE_CAS_USERS,
        ROLE_CAS_SUBROGATIONS,

        ROLE_CHECK_USERS, ROLE_CHECK_TENANTS,
        ROLE_GET_ALL_ACCESS_CONTRACTS,

        ROLE_GET_OPERATIONS,
        ROLE_GET_FILE_OPERATION,

        ROLE_CREATE_ACCESS_CONTRACTS,
        ROLE_GET_ACCESS_CONTRACTS,
        ROLE_UPDATE_ACCESS_CONTRACTS,

        ROLE_CREATE_INGEST_CONTRACTS,
        ROLE_GET_INGEST_CONTRACTS,
        ROLE_UPDATE_INGEST_CONTRACTS,
        ROLE_GET_FILLING_PLAN_ACCESS,

        ROLE_GET_AGENCIES,
        ROLE_UPDATE_AGENCIES,
        ROLE_CREATE_AGENCIES,
        ROLE_DELETE_AGENCIES,
        ROLE_EXPORT_AGENCIES,

        ROLE_GET_FILE_FORMATS,
        ROLE_DELETE_FILE_FORMATS,
        ROLE_CREATE_FILE_FORMATS,
        ROLE_UPDATE_FILE_FORMATS,


        ROLE_GET_CONTEXTS,
        ROLE_CREATE_CONTEXTS,
        ROLE_UPDATE_CONTEXTS,

        ROLE_GET_SECURITY_PROFILES,
        ROLE_UPDATE_SECURITY_PROFILES,
        ROLE_CREATE_SECURITY_PROFILES,
        ROLE_DELETE_SECURITY_PROFILES,

        ROLE_GET_RULES,
        ROLE_CREATE_RULES,
        ROLE_UPDATE_RULES,
        ROLE_DELETE_RULES,

        ROLE_GET_ONTOLOGIES,
        ROLE_CREATE_ONTOLOGIES,
        ROLE_UPDATE_ONTOLOGIES,
        ROLE_DELETE_ONTOLOGIES,

        ROLE_GET_AUDITS,
        ROLE_RUN_AUDITS,
        ROLE_RUN_PROBATIVE_VALUE,

        ROLE_GET_ALL_INGEST,
        ROLE_GET_INGEST,
        ROLE_CREATE_INGEST,
        ROLE_LOGBOOKS,

        ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE,
        ROLE_EXPORT_DIP,
        ROLE_TRANSFER_REQUEST,
        ROLE_ELIMINATION,
        ROLE_COMPUTED_INHERITED_RULES,
        ROLE_TRANSFER_ACKNOWLEDGMENT,

        ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE,
        ROLE_GET_HOLDING_FILLING_SCHEME_ROLE,
        ROLE_GET_ALL_HOLDING_FILLING_SCHEME_ROLE,

        ROLE_GET_UNITS,

        ROLE_GET_EXTERNAL_PARAMS,

        ROLE_UPDATE_LOGBOOK_OPERATION,
        ROLE_GET_LOGBOOK_OPERATION,
        ROLE_GET_ALL_LOGBOOK_OPERATION,

        ROLE_GET_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
        ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
        ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
        ROLE_GET_ALL_LOGBOOK_OPERATION,

        ROLE_GET_ACCESSION_REGISTER_DETAIL,
        ROLE_RECLASSIFICATION,

        ROLE_CREATE_TRANSACTIONS,
        ROLE_UPDATE_TRANSACTIONS,
        ROLE_GET_TRANSACTIONS,
        ROLE_DELETE_PROJECTS,

        ROLE_GET_PROJECTS,
        ROLE_CREATE_PROJECTS,
        ROLE_UPDATE_PROJECTS,

        ROLE_CLOSE_TRANSACTIONS,
        ROLE_SEND_TRANSACTIONS,
        ROLE_UPDATE_UNITS_METADATA,

        ROLE_GET_ACCESSION_REGISTER_DETAIL,

        ROLE_GET_MANAGEMENT_CONTRACT,
        ROLE_CREATE_MANAGEMENT_CONTRACT,
        ROLE_DELETE_MANAGEMENT_CONTRACT,
        ROLE_UPDATE_MANAGEMENT_CONTRACT,

        ROLE_GET_ACCESSION_REGISTER_DETAIL,
        ROLE_RECLASSIFICATION,
        ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT_ROLE

            );

    //@formatter:on

    public static List<String> getAdminVitamUIRoleNames() {
        return new ArrayList<>(ADMIN_VITAMUI_ROLES);
    }

    public static List<String> getAllRoleNames() {
        return new ArrayList<>(ROLE_NAMES);
    }

    public static List<Role> getAdminVitamUIRoles() {
        return ADMIN_VITAMUI_ROLES.stream().map(Role::new).collect(Collectors.toList());
    }

    public static List<Role> getAllRoles() {
        return ROLE_NAMES.stream().map(Role::new).collect(Collectors.toList());
    }

    public static boolean checkIfRoleNameExists(final List<String> roleNames) {
        return roleNames.stream().allMatch(role -> ROLE_NAMES.contains(role));
    }

    public static boolean checkIfRoleExists(final List<Role> roles) {
        return roles.stream().allMatch(role -> ROLE_NAMES.contains(role.getName()));
    }

    public static List<String> getServicesByName(final String... serviceName) {
        return ROLE_NAMES.stream()
            .filter(role -> StringUtils.endsWithAny(role, serviceName))
            .collect(Collectors.toList());
    }
}
