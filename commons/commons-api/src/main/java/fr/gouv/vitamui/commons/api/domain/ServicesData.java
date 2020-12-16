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
package fr.gouv.vitamui.commons.api.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.commons.api.CommonConstants.CHECK_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.CREATE_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.DELETE_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.EXPORT_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.IMPORT_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.GET_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.UPDATE_ME_ROLE_PREFIX;
import static fr.gouv.vitamui.commons.api.CommonConstants.UPDATE_ROLE_PREFIX;

/**
 * All the services.
 *
 *
 */
public class ServicesData {

    //------------------------------------ USERS -------------------------------------------
    public static final String SERVICE_USERS = "USERS";

    public static final String ROLE_GET_USERS = GET_ROLE_PREFIX + SERVICE_USERS;

    public static final String ROLE_CREATE_USERS = CREATE_ROLE_PREFIX + SERVICE_USERS;

    public static final String ROLE_UPDATE_USERS = UPDATE_ROLE_PREFIX + SERVICE_USERS;

    public static final String ROLE_UPDATE_STANDARD_USERS = UPDATE_ROLE_PREFIX + "STANDARD_" + SERVICE_USERS;

    public static final String ROLE_GENERIC_USERS = "ROLE_GENERIC_" + SERVICE_USERS;

    public static final String ROLE_ANONYMIZATION_USERS = "ROLE_ANONYMIZATION_" + SERVICE_USERS;

    public static final String ROLE_MFA_USERS = "ROLE_MFA_" + SERVICE_USERS;

    public static final String ROLE_UPDATE_ME_USERS = UPDATE_ME_ROLE_PREFIX + SERVICE_USERS;

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

    public static final String ROLE_GET_TENANTS = GET_ROLE_PREFIX + SERVICE_TENANTS;

    public static final String ROLE_GET_ALL_TENANTS = GET_ROLE_PREFIX + "ALL_" + SERVICE_TENANTS;

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

    public static final String ROLE_GET_GROUPS = GET_ROLE_PREFIX + SERVICE_GROUPS;

    public static final String ROLE_GET_ALL_GROUPS = GET_ROLE_PREFIX + "ALL_" + SERVICE_GROUPS;

    public static final String ROLE_CREATE_GROUPS = CREATE_ROLE_PREFIX + SERVICE_GROUPS;

    public static final String ROLE_UPDATE_GROUPS = UPDATE_ROLE_PREFIX + SERVICE_GROUPS;

    public static final String ROLE_DELETE_GROUPS = DELETE_ROLE_PREFIX + SERVICE_GROUPS;

    //------------------------------------ PROFILES -------------------------------------------

    public static final String SERVICE_PROFILES = "PROFILES";

    public static final String ROLE_GET_PROFILES = GET_ROLE_PREFIX + SERVICE_PROFILES;

    public static final String ROLE_CREATE_PROFILES = CREATE_ROLE_PREFIX + SERVICE_PROFILES;

    public static final String ROLE_UPDATE_PROFILES = UPDATE_ROLE_PREFIX + SERVICE_PROFILES;

    public static final String ROLE_DELETE_PROFILES = DELETE_ROLE_PREFIX + SERVICE_PROFILES;

    public static final String ROLE_GET_PROFILES_ALL_TENANTS = GET_ROLE_PREFIX + SERVICE_PROFILES + "_ALL_" + SERVICE_TENANTS;

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

    public static final String ROLE_CAS_SUBROGATIONS = ROLE_PREFIX + "CAS_SUBROGATIONS";

    public static final String ROLE_LOGBOOKS = "ROLE_LOGBOOKS";

    //------------------------------------ TECHNICAL CHECKS -------------------------------------------

    public static final String ROLE_CHECK_TENANTS = CHECK_ROLE_PREFIX + SERVICE_TENANTS;

    public static final String ROLE_CHECK_USERS = CHECK_ROLE_PREFIX + SERVICE_USERS;


    //------------------------------------ ACCOUNTS -------------------------------------------

    public static final String SERVICE_ACCOUNTS = "ACCOUNTS";

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

    //------------------------------------ INGEST CONTRACT -----------------------------------------

    public static final String SERVICE_INGEST_CONTRACT = "INGEST_CONTRACTS";

    public static final String ROLE_CREATE_INGEST_CONTRACTS = CREATE_ROLE_PREFIX + SERVICE_INGEST_CONTRACT;

    public static final String ROLE_GET_INGEST_CONTRACTS = GET_ROLE_PREFIX + SERVICE_INGEST_CONTRACT;

    public static final String ROLE_UPDATE_INGEST_CONTRACTS = UPDATE_ROLE_PREFIX + SERVICE_INGEST_CONTRACT;

    //------------------------------------ AGENCIES -----------------------------------------

    public static final String SERVICE_AGENCIES = "AGENCIES";

    public static final String ROLE_GET_AGENCIES = GET_ROLE_PREFIX  + SERVICE_AGENCIES;

    public static final String ROLE_CREATE_AGENCIES = CREATE_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_UPDATE_AGENCIES = UPDATE_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_DELETE_AGENCIES = DELETE_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_EXPORT_AGENCIES = EXPORT_ROLE_PREFIX + SERVICE_AGENCIES;

    public static final String ROLE_IMPORT_AGENCIES = IMPORT_ROLE_PREFIX + SERVICE_AGENCIES;

    //---------------------------------- FILE FORMATS --------------------------------------------

    public static final String SERVICE_FILE_FORMATS = "FILE_FORMATS";

    public static final String ROLE_GET_FILE_FORMATS = GET_ROLE_PREFIX  + SERVICE_FILE_FORMATS;

    public static final String ROLE_CREATE_FILE_FORMATS = CREATE_ROLE_PREFIX+ SERVICE_FILE_FORMATS;

    public static final String ROLE_UPDATE_FILE_FORMATS = UPDATE_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    public static final String ROLE_DELETE_FILE_FORMATS = DELETE_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    public static final String ROLE_EXPORT_FILE_FORMATS = EXPORT_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    public static final String ROLE_IMPORT_FILE_FORMATS = IMPORT_ROLE_PREFIX + SERVICE_FILE_FORMATS;

    //------------------------------------ CONTEXTS -----------------------------------------

    public static final String SERVICE_CONTEXTS = "CONTEXTS";

    public static final String ROLE_GET_CONTEXTS = GET_ROLE_PREFIX+ SERVICE_CONTEXTS;

    public static final String ROLE_CREATE_CONTEXTS = CREATE_ROLE_PREFIX  + SERVICE_CONTEXTS;

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

    //------------------------------------ ONTOLOGIES -----------------------------------------

    public static final String SERVICE_ONTOLOGIES = "ONTOLOGIES";

    public static final String ROLE_GET_ONTOLOGIES = CREATE_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    public static final String ROLE_CREATE_ONTOLOGIES = GET_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    public static final String ROLE_DELETE_ONTOLOGIES = DELETE_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    public static final String ROLE_IMPORT_ONTOLOGIES = IMPORT_ROLE_PREFIX + SERVICE_ONTOLOGIES;

    //------------------------------------- OPERATIONS --------------------------------------------

    public static final String SERVICE_OPERATIONS = "OPERATIONS";

    public static final String ROLE_GET_OPERATIONS = GET_ROLE_PREFIX + SERVICE_OPERATIONS;

    public static final String ROLE_GET_FILE_OPERATION = GET_ROLE_PREFIX + "FILE_" + SERVICE_OPERATIONS;

    //------------------------------------- AUDITS --------------------------------------------

    public static final String SERVICE_AUDITS = "AUDITS";

    public static final String ROLE_RUN_AUDITS = ROLE_PREFIX + "RUN_" + SERVICE_AUDITS;

    //------------------------------------- PROBATIVE_VALUE --------------------------------------------

    public static final String SERVICE_PROBATIVE_VALUE = "PROBATIVE_VALUE";

    public static final String ROLE_RUN_PROBATIVE_VALUE = "ROLE_RUN_" + SERVICE_PROBATIVE_VALUE;


    /** Management Contracts and Profiles are used in IngestContract forms **/
    //----------------------------------- MANAGEMENT CONTRACTS --------------------------------------

    public static final String ROLE_GET_MANAGEMENT_CONTRACTS = ROLE_PREFIX + "GET_MANAGEMENT_CONTRACTS";

    //------------------------------ PROFILES (ARCHIVE PROFILES) --------------------------------------

    public static final String ROLE_GET_ARCHIVE_PROFILES = ROLE_PREFIX + "GET_ARCHIVE_PROFILES";

    //------------------------------------ INGESTS -----------------------------------------

    public static final String SERVICE_INGEST = "INGEST";

    public static final String ROLE_CREATE_INGEST = CREATE_ROLE_PREFIX + SERVICE_INGEST;
    public static final String ROLE_GET_INGEST = GET_ROLE_PREFIX + SERVICE_INGEST;
    public static final String ROLE_GET_ALL_INGEST = GET_ROLE_PREFIX + "ALL_" + SERVICE_INGEST;


    //------------------------------------ ARCHIVES -----------------------------------------

    public static final String SERVICE_ARCHIVE = "ARCHIVE_SEARCH";

    public static final String ROLE_CREATE_ARCHIVE = CREATE_ROLE_PREFIX + SERVICE_ARCHIVE;
    public static final String ROLE_GET_ARCHIVE = GET_ROLE_PREFIX + SERVICE_ARCHIVE;
    public static final String ROLE_GET_ALL_ARCHIVE = GET_ROLE_PREFIX + "ALL_" + SERVICE_ARCHIVE;


    //------------------------------------ API TREES & PLANS -----------------------------------------

    public static final String SERVICE_HOLDING_FILLING_SCHEME_ROLE = "HOLDING_FILLING_SCHEME";

    public static final String ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE = CREATE_ROLE_PREFIX + SERVICE_HOLDING_FILLING_SCHEME_ROLE;
    public static final String ROLE_GET_HOLDING_FILLING_SCHEME_ROLE = GET_ROLE_PREFIX + SERVICE_HOLDING_FILLING_SCHEME_ROLE;
    public static final String ROLE_GET_ALL_HOLDING_FILLING_SCHEME_ROLE = GET_ROLE_PREFIX + "ALL_" + SERVICE_HOLDING_FILLING_SCHEME_ROLE;


    
    //------------------------------------ UNITS -----------------------------------------

    public static final String SERVICE_UNITS = "UNITS";

    public static final String ROLE_GET_UNITS = GET_ROLE_PREFIX + SERVICE_UNITS;
    
    //------------------------------------ EXTERNAL PARAMETERS -------------------------------------------
    public static final String SERVICE_EXTERNAL_PARAMS = "EXTERNAL_PARAMS";
    
    public static final String ROLE_GET_EXTERNAL_PARAMS = GET_ROLE_PREFIX + SERVICE_EXTERNAL_PARAMS;
    
    //@formatter:off

    /**
     * List of the admin roles for the VITAMUI application.
     */

    private static final List<String> ADMIN_VITAMUI_ROLES = Arrays.asList(
            ROLE_GET_CUSTOMERS,
            ROLE_CREATE_CUSTOMERS,
            ROLE_UPDATE_CUSTOMERS,

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
            ROLE_DELETE_ONTOLOGIES,

            ROLE_RUN_AUDITS,
            ROLE_GET_OPERATIONS,
            ROLE_RUN_PROBATIVE_VALUE,

            ROLE_GET_ALL_INGEST,
            ROLE_GET_INGEST,
            ROLE_CREATE_INGEST,

            ROLE_LOGBOOKS,

            ROLE_GET_ALL_ARCHIVE,
            ROLE_GET_ARCHIVE,
            ROLE_CREATE_ARCHIVE,

            ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE,
            ROLE_GET_HOLDING_FILLING_SCHEME_ROLE,
            ROLE_GET_ALL_HOLDING_FILLING_SCHEME_ROLE,
            
            ROLE_GET_UNITS,
            
            ROLE_GET_EXTERNAL_PARAMS
            );

    /**
     * List of all the roles in the VITAMUI application (including the admin roles present in the ADMIN_VITAMUI_ROLES list)
     */

    private static final List<String> ROLE_NAMES = Arrays.asList(
            ROLE_GET_USERS, ROLE_CREATE_USERS,
            ROLE_UPDATE_USERS,
            ROLE_UPDATE_STANDARD_USERS,
            ROLE_GENERIC_USERS,
            ROLE_MFA_USERS,
            ROLE_ANONYMIZATION_USERS,
            ROLE_UPDATE_ME_USERS,


            ROLE_GET_CUSTOMERS,
            ROLE_CREATE_CUSTOMERS,
            ROLE_UPDATE_CUSTOMERS,

            ROLE_GET_TENANTS,
            ROLE_CREATE_TENANTS,
            ROLE_CREATE_TENANTS_ALL_CUSTOMERS,
            ROLE_UPDATE_TENANTS,
            ROLE_GET_ALL_TENANTS,
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
            ROLE_DELETE_ONTOLOGIES,

            ROLE_RUN_AUDITS,
            ROLE_RUN_PROBATIVE_VALUE,

            ROLE_GET_ALL_INGEST,
            ROLE_GET_INGEST,
            ROLE_CREATE_INGEST,
            ROLE_LOGBOOKS,

            ROLE_GET_ALL_ARCHIVE,
            ROLE_GET_ARCHIVE,
            ROLE_CREATE_ARCHIVE,

            ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE,
            ROLE_GET_HOLDING_FILLING_SCHEME_ROLE,
            ROLE_GET_ALL_HOLDING_FILLING_SCHEME_ROLE,

            ROLE_GET_UNITS,
            
            ROLE_GET_EXTERNAL_PARAMS
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
        return ROLE_NAMES.stream().filter(role -> StringUtils.endsWithAny(role, serviceName)).collect(Collectors.toList());
    }
}
