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
package fr.gouv.vitamui.iam.auth.contract;

/**
 * Paths of the authentication contract exposed by IAM.
 *
 * The URL values keep the historic {@code /cas} segment: changing them would break the REST contract
 * with the authentication servers already deployed. Only the constant names stop naming a product,
 * since the contract is no longer specific to Apereo CAS.
 */
public final class AuthContractApi {

    public static final String V1_AUTH_URL = "/iam/v1/cas";

    public static final String LOGIN_PATH = "/login";

    public static final String LOGOUT_PATH = "/logout";

    public static final String CHANGE_PASSWORD_PATH = "/password/change";

    public static final String USERS_PATH = "/users";

    public static final String USERS_PROVISIONING_PATH = "/provisioning";

    public static final String CUSTOMERS_PATH = "/customers";

    public static final String SUBROGATIONS_PATH = "/subrogations";

    public static final String HRD_PATH = "/hrd";

    public static final String PASSWORD_POLICY_PATH = "/password/policy";

    public static final String SUBROGATION_VALIDATE_PATH = "/subrogations/validate";

    public static final String PRINCIPAL_ATTRIBUTES_PATH = "/users/principal-attributes";

    private AuthContractApi() {
        // constants only
    }
}
