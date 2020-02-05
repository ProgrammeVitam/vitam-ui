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
package fr.gouv.vitamui.iam.internal.server.common;

import java.util.List;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;

/**
 *
 * Constants for API IAM Internal.
 *
 *
 */
public final class ApiIamInternalConstants {

    public static final String ADMIN_CLIENT_ROOT = "ADMIN_CLIENT_ROOT";

    private static final List<String> ACCOUNT_ROLES = VitamUIUtils.listOf(ServicesData.ROLE_UPDATE_ME_USERS);

    private static final List<String> USERS_ROLES = VitamUIUtils.listOf(ServicesData.ROLE_GET_USERS, ServicesData.ROLE_CREATE_USERS,
            ServicesData.ROLE_UPDATE_USERS, ServicesData.ROLE_UPDATE_STANDARD_USERS, ServicesData.ROLE_MFA_USERS, ServicesData.ROLE_ANONYMIZATION_USERS,
            ServicesData.ROLE_GENERIC_USERS, ServicesData.ROLE_GET_GROUPS);

    private static final List<String> GROUPS_ROLES = VitamUIUtils.listOf(ServicesData.ROLE_GET_GROUPS, ServicesData.ROLE_CREATE_GROUPS,
            ServicesData.ROLE_UPDATE_GROUPS, ServicesData.ROLE_DELETE_GROUPS, ServicesData.ROLE_GET_PROFILES, ServicesData.ROLE_GET_PROFILES_ALL_TENANTS);

    private static final List<String> PROFILES_ROLES = VitamUIUtils.listOf(ServicesData.ROLE_GET_PROFILES, ServicesData.ROLE_CREATE_PROFILES,
            ServicesData.ROLE_UPDATE_PROFILES, ServicesData.ROLE_DELETE_PROFILES, ServicesData.ROLE_GET_GROUPS);

    private static final List<String> HIERARCHY_ROLES = VitamUIUtils.listOf(ServicesData.ROLE_GET_PROFILES, ServicesData.ROLE_CREATE_PROFILES,
            ServicesData.ROLE_UPDATE_PROFILES, ServicesData.ROLE_DELETE_PROFILES);

    public static final String ADMIN_CLIENT_PREFIX_EMAIL = "admin";

    public static final String ADMIN_CLIENT_LASTNAME = "ADMIN";

    public static final String ADMIN_CLIENT_FIRSTNAME = "Admin";

    public static final String ADMIN_LEVEL = "";

    public static final String SUPPORT_LEVEL = "SUPPORT";

    public static final String LEVEL_VALID_REGEXP = "(^[A-Z0-9]+(.[A-Z0-9]+)*$)|^$";

    public static final String ACCOUNT_PROFILE_DESCRIPTION = "Profil de l'application Mon Compte";

    public static final String USERS_PROFILE_DESCRIPTION = "Profil de l'application de gestion des utilisateurs";

    public static final String GROUPS_PROFILE_DESCRIPTION = "Profil de l'application de gestion des groupes";

    public static final String PROFILE_DESCRIPTION = "Profil de l'application de gestion des profils utilisateurs";

    public static final String HIERARCHY_PROFILE_NAME = "Hierarchy Profiles";

    public static final String HIERARCHY_PROFILE_DESCRIPTION = "Profil de l'application de gestion des hiérarchies de profils";

    public static final String EMAIL_VALID_REGEXP = "^[_a-z0-9]+(((\\.|-)[_a-z0-9]+))*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$";

    public static final String PHONE_NUMBER_VALID_REGEXP = "^[+]{1}[0-9]{11,12}$";

    private ApiIamInternalConstants() {
        // do nothing
    }

    /**
     * Retrieve Account ROles.
     * @return
     */
    public static List<String> getAccountRoles() {
        return ACCOUNT_ROLES;
    }

    /**
     * Retrieve Users Roles.
     * @return
     */
    public static List<String> getUsersRoles() {
        return USERS_ROLES;
    }

    /**
     * Retrieve Groups Roles.
     * @return
     */
    public static List<String> getGroupsRoles() {
        return GROUPS_ROLES;
    }

    /**
     * Retrieve Profiles Roles.
     * @return
     */
    public static List<String> getProfilesRoles() {
        return PROFILES_ROLES;
    }

    /**
     * Retrieve Hierarchy Roles.
     * @return
     */
    public static List<String> getHierarchyRoles() {
        return HIERARCHY_ROLES;
    }

}
