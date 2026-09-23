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

import fr.gouv.vitamui.cas.model.CustomerModel;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.iam.auth.contract.HrdEntryDto;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Comparator;
import java.util.List;

import static fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTED;
import static fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTION_VIEW;

/**
 * This class lists users matching provided login email:
 * - if a single user is found ==> continue to dispatcher
 * - if multiple users found ==> redirect to customer selection page
 * - if no user found : act as if it exists (to avoid account existence
 * disclosure)
 */
@Slf4j
public class ListCustomersAction extends AbstractAction {

    public static final String BAD_CONFIGURATION = "badConfiguration";

    private final CasApi casApi;

    public ListCustomersAction(final CasApi casApi) {
        this.casApi = casApi;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        var flowScope = requestContext.getFlowScope();

        if (isSubrogationMode(flowScope)) {
            return processSubrogationRequest(flowScope);
        } else {
            return processEmailInput(requestContext, flowScope);
        }
    }

    private Event processSubrogationRequest(MutableAttributeMap<Object> flowScope) {
        // We came from subrogation validation (emailForm)
        String surrogateEmail = (String) flowScope.get(Constants.FLOW_SURROGATE_EMAIL);
        String surrogateCustomerId = (String) flowScope.get(Constants.FLOW_SURROGATE_CUSTOMER_ID);
        String superUserEmail = (String) flowScope.get(Constants.FLOW_LOGIN_EMAIL);
        String superUserCustomerId = (String) flowScope.get(Constants.FLOW_LOGIN_CUSTOMER_ID);

        LOGGER.debug(
            "Subrogation of '{}' (customerId '{}') by super admin '{}' (customerId '{}')",
            surrogateEmail,
            surrogateCustomerId,
            superUserEmail,
            superUserCustomerId
        );

        ParameterChecker.checkParameter(
            "Missing subrogation params",
            surrogateEmail,
            surrogateCustomerId,
            superUserEmail,
            superUserCustomerId
        );

        boolean providerConfigured = casApi
            .resolveHrd(superUserEmail)
            .stream()
            .anyMatch(
                entry -> superUserCustomerId.equals(entry.getCustomerId()) && entry.getIdentityProviderId() != null
            );
        if (!providerConfigured) {
            LOGGER.error(
                "No provider found for superUserEmail: {} / superUserCustomerId: {}",
                superUserEmail,
                superUserCustomerId
            );
            return new Event(this, BAD_CONFIGURATION);
        }

        return handleSingleAuthenticationProvider(flowScope, superUserEmail, superUserCustomerId);
    }

    private Event processEmailInput(RequestContext requestContext, MutableAttributeMap<Object> flowScope) {
        UsernamePasswordCredential credential = WebUtils.getCredential(
            requestContext,
            UsernamePasswordCredential.class
        );
        String username = credential.getUsername().toLowerCase().trim();

        LOGGER.debug("User provided login of '{}'", username);

        List<HrdEntryDto> entries = casApi.resolveHrd(username);

        if (entries.isEmpty()) {
            LOGGER.warn("No provider found for email: '{}'", username);
            return new Event(this, BAD_CONFIGURATION);
        }
        if (entries.size() == 1) {
            return handleSingleAuthenticationProvider(flowScope, username, entries.getFirst().getCustomerId());
        }
        return handleMultipleAuthenticationProviders(flowScope, username, entries);
    }

    private Event handleSingleAuthenticationProvider(
        MutableAttributeMap<Object> flowScope,
        String loginEmail,
        String customerId
    ) {
        LOGGER.debug(
            "User '{}' has a single available customer {}. No need for customer selection page",
            loginEmail,
            customerId
        );

        flowScope.put(Constants.FLOW_LOGIN_EMAIL, loginEmail);
        flowScope.put(Constants.FLOW_LOGIN_CUSTOMER_ID, customerId);
        flowScope.remove(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST);

        return new Event(this, TRANSITION_TO_CUSTOMER_SELECTED);
    }

    @NotNull
    private Event handleMultipleAuthenticationProviders(
        MutableAttributeMap<Object> flowScope,
        String username,
        List<HrdEntryDto> entries
    ) {
        List<CustomerModel> customerToSelect = entries
            .stream()
            .map(entry -> new CustomerModel(entry.getCustomerId(), entry.getCustomerCode(), entry.getCustomerName()))
            .sorted(Comparator.comparing(CustomerModel::code))
            .toList();

        LOGGER.debug(
            "Redirecting user with login of '{}' to customer selection page. Available customers: {}",
            username,
            customerToSelect
        );

        flowScope.put(Constants.FLOW_LOGIN_EMAIL, username);
        flowScope.remove(Constants.FLOW_LOGIN_CUSTOMER_ID);
        flowScope.put(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST, customerToSelect);

        return new Event(this, TRANSITION_TO_CUSTOMER_SELECTION_VIEW);
    }

    private static boolean isSubrogationMode(MutableAttributeMap<Object> flowScope) {
        return flowScope.contains(Constants.FLOW_SURROGATE_EMAIL);
    }
}
