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
package fr.gouv.vitamui.cas.util;

/**
 * Constants.
 *
 * @sicne 0.1.0
 */
public abstract class Constants {

    public static final String PROVIDED_USERNAME = "providedUsername";
    public static final String SHOW_SURROGATE_CUSTOMER_NAME = "surrogateCustomerName";
    public static final String SHOW_SURROGATE_CUSTOMER_CODE = "surrogateCustomerCode";

    public static final String LOGIN_USER_EMAIL_PARAM = "username";
    public static final String LOGIN_SURROGATE_EMAIL_PARAM = "surrogateEmail";
    public static final String LOGIN_SURROGATE_CUSTOMER_ID_PARAM = "surrogateCustomerId";
    public static final String LOGIN_SUPER_USER_EMAIL_PARAM = "superUserEmail";
    public static final String LOGIN_SUPER_USER_CUSTOMER_ID_PARAM = "superUserCustomerId";

    public static final String RESET_PWD_CUSTOMER_ID_ATTR = "customerIdAttr";
    public static final String SELECT_CUSTOMER_ID_PARAM = "customerId";

    public static final String FLOW_SURROGATE_EMAIL = "surrogateEmail";
    public static final String FLOW_SURROGATE_CUSTOMER_ID = "surrogateCustomerId";
    public static final String FLOW_LOGIN_EMAIL = "loginEmail";
    public static final String FLOW_LOGIN_CUSTOMER_ID = "loginCustomerId";
    public static final String FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST = "availableCustomerList";

    // web:
    public static final String PORTAL_URL = "portalUrl";

    public static final String VITAM_UI_FAVICON = "vitamuiFavicon";

    public static final String PASSWORD_CUSTOM_CONSTRAINTS = "passwordCustomConstraints";

    public static final String PASSWORD_DEFAULT_CONSTRAINTS = "passwordAnssiConstraints";

    public static final String MAX_OLD_PASSWORD = "maxOldPassword";

    public static final String CHECK_OCCURRENCE = "checkOccurrence";

    public static final String OCCURRENCE_CHAR_NUMBERS = "occurrencesCharsNumber";
}
