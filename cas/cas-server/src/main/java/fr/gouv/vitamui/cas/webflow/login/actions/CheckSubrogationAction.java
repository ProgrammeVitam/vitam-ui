/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.cas.webflow.login.actions;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Action to check if subrogation parameters are present in the request.
 * If present, it populates the flow scope and returns 'subrogation'.
 * Otherwise, it returns 'proceed'.
 */
@Slf4j
@RequiredArgsConstructor
public class CheckSubrogationAction extends AbstractAction {

    public static final String SUBROGATION = "subrogation";
    public static final String PROCEED = "proceed";

    private static final Pattern EMAIL_VALID_REGEXP = Pattern.compile(
        "^[_A-Za-z0-9]+(((\\.|-)[_A-Za-z0-9]+))*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
    );
    private static final Pattern CUSTOMER_ID_VALIDATION_PATTERN = Pattern.compile("^\\w+$");

    private final CasApi casApi;

    @Override
    protected Event doExecute(RequestContext context) {
        String surrogateEmail = context.getRequestParameters().get(Constants.LOGIN_SURROGATE_EMAIL_PARAM);
        String surrogateCustomerId = context.getRequestParameters().get(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM);
        String superUserEmail = context.getRequestParameters().get(Constants.LOGIN_SUPER_USER_EMAIL_PARAM);
        String superUserCustomerId = context.getRequestParameters().get(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM);

        if (StringUtils.isNoneBlank(surrogateEmail, surrogateCustomerId, superUserEmail, superUserCustomerId)) {
            try {
                validateEmail(surrogateEmail);
                validateEmail(superUserEmail);
                validateCustomerId(surrogateCustomerId);
                validateCustomerId(superUserCustomerId);

                LOGGER.debug(
                    "Subrogation parameters validated: surrogateEmail={}, surrogateCustomerId={}, superUserEmail={}, superUserCustomerId={}",
                    surrogateEmail,
                    surrogateCustomerId,
                    superUserEmail,
                    superUserCustomerId
                );

                var flowScope = context.getFlowScope();
                flowScope.put(Constants.FLOW_SURROGATE_EMAIL, surrogateEmail);
                flowScope.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, surrogateCustomerId);
                flowScope.put(Constants.FLOW_LOGIN_EMAIL, superUserEmail);
                flowScope.put(Constants.FLOW_LOGIN_CUSTOMER_ID, superUserCustomerId);

                // Fetch surrogate customer info for display in subrogation validation mire
                CustomerDto surrogateCustomer = casApi
                    .getCustomersByIds(List.of(surrogateCustomerId))
                    .stream()
                    .findFirst()
                    .orElseThrow(
                        () -> new IllegalArgumentException("Invalid surrogateCustomerId: '" + surrogateCustomerId + "'")
                    );

                flowScope.put(Constants.SHOW_SURROGATE_CUSTOMER_CODE, surrogateCustomer.getCode());
                flowScope.put(Constants.SHOW_SURROGATE_CUSTOMER_NAME, surrogateCustomer.getName());

                return new Event(this, SUBROGATION);
            } catch (Exception e) {
                LOGGER.error("Validation of subrogation parameters failed", e);
                // If validation fails, we treat it as a normal login request
            }
        }

        return new Event(this, PROCEED);
    }

    private void validateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Null email");
        }
        if (!EMAIL_VALID_REGEXP.matcher(email).matches()) {
            throw new IllegalArgumentException("email : '" + email + "' format is not allowed");
        }
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Null customerId");
        }
        if (!CUSTOMER_ID_VALIDATION_PATTERN.matcher(customerId).matches()) {
            throw new IllegalArgumentException("Invalid customerId: '" + customerId + "'");
        }
    }
}
