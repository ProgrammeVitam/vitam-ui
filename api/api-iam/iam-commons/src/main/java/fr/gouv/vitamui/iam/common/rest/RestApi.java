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
package fr.gouv.vitamui.iam.common.rest;

/**
 * The URLs of the REST API.
 *
 *
 */
public abstract class RestApi {

    public static final String STATUS_URL = "/status";

    public static final String AUTOTEST_URL = "/autotest";

    public static final String V1_CUSTOMERS_URL = "/iam/v1/customers";

    public static final String V1_TENANTS_URL = "/iam/v1/tenants";

    public static final String V1_OWNERS_URL = "/iam/v1/owners";

    public static final String V1_PROVIDERS_URL = "/iam/v1/providers";

    public static final String V1_USERS_URL = "/iam/v1/users";

    public static final String V1_ACCOUNTS_URL = "/iam/v1/accounts";

    public static final String V1_GROUPS_URL = "/iam/v1/groups";

    public static final String V1_PROFILES_URL = "/iam/v1/profiles";

    public static final String V1_SUBROGATIONS_URL = "/iam/v1/subrogations";

    public static final String V1_APPLICATIONS_URL = "/iam/v1/applications";

    public static final String V1_CAS_URL = "/iam/v1/cas";

    public static final String CAS_LOGIN_PATH = "/login";

    public static final String CAS_LOGOUT_PATH = "/logout";

    public static final String CAS_CHANGE_PASSWORD_PATH = "/password/change";

    public static final String CAS_USERS_PATH = "/users";

    public static final String CAS_SUBROGATIONS_PATH = "/subrogations";

    public static final String V1_IAM_URL = "/iam/v1";
    
    public static final String V1_EXTERNAL_PARAMETERS_URL = "/iam/v1/externalparameters";

    private RestApi() {
        // do nothing
    }
}
