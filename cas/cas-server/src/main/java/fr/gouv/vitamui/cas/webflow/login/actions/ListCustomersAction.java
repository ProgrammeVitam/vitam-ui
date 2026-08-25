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
import fr.gouv.vitamui.iam.common.dto.cas.OrganizationCandidateDto;
import fr.gouv.vitamui.iam.common.dto.cas.ResolvedIdentityProviderDto;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTED;
import static fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTION_VIEW;

/**
 * This class asks IAM which organizations claim the provided login email:
 * - if a single one is returned ==> continue to dispatcher
 * - if several are returned ==> redirect to customer selection page
 * - if none is returned ==> bad configuration
 */
@Slf4j
public class ListCustomersAction extends AbstractAction {

    public static final String BAD_CONFIGURATION = "badConfiguration";

    private final CasApi casApi;

    public ListCustomersAction(final CasApi casApi) {
        this.casApi = casApi;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws IOException {
        var flowScope = requestContext.getFlowScope();

        if (isSubrogationMode(flowScope)) {
            return processSubrogationRequest(flowScope);
        } else {
            return processEmailInput(requestContext, flowScope);
        }
    }

    private Event processSubrogationRequest(MutableAttributeMap<Object> flowScope) throws IOException {
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

        ResolvedIdentityProviderDto resolvedProvider = casApi.resolveIdentityProvider(
            superUserEmail,
            superUserCustomerId
        );
        if (resolvedProvider.getIdentityProviderId() == null) {
            LOGGER.error(
                "No provider found for superUserEmail: {} / superUserCustomerId: {}",
                superUserEmail,
                superUserCustomerId
            );
            return new Event(this, BAD_CONFIGURATION);
        }

        return handleSingleOrganization(flowScope, superUserEmail, superUserCustomerId);
    }

    private Event processEmailInput(RequestContext requestContext, MutableAttributeMap<Object> flowScope) {
        UsernamePasswordCredential credential = WebUtils.getCredential(
            requestContext,
            UsernamePasswordCredential.class
        );
        String username = credential.getUsername().toLowerCase().trim();

        LOGGER.debug("User provided login of '{}'", username);

        List<OrganizationCandidateDto> claimingOrganizations = casApi.resolveOrganizations(username);

        if (claimingOrganizations.isEmpty()) {
            LOGGER.warn("No organization claims the login of '{}'", username);
            return new Event(this, BAD_CONFIGURATION);
        }

        if (claimingOrganizations.size() == 1) {
            return handleSingleOrganization(
                flowScope,
                username,
                claimingOrganizations.getFirst().getCustomerId()
            );
        }

        return handleSeveralOrganizations(flowScope, username, claimingOrganizations);
    }

    private Event handleSingleOrganization(
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

    private Event handleSeveralOrganizations(
        MutableAttributeMap<Object> flowScope,
        String username,
        List<OrganizationCandidateDto> claimingOrganizations
    ) {
        List<CustomerModel> customerToSelect = toDistinctCustomerModels(claimingOrganizations);

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

    private List<CustomerModel> toDistinctCustomerModels(final List<OrganizationCandidateDto> claimingOrganizations) {
        Map<String, CustomerModel> byCustomerId = new LinkedHashMap<>();
        claimingOrganizations.forEach(organization ->
            byCustomerId.computeIfAbsent(
                organization.getCustomerId(),
                customerId ->
                    new CustomerModel()
                        .setCustomerId(customerId)
                        .setCode(organization.getCode())
                        .setName(organization.getName())
            )
        );
        return byCustomerId.values().stream().sorted(Comparator.comparing(CustomerModel::getCode)).toList();
    }

    private static boolean isSubrogationMode(MutableAttributeMap<Object> flowScope) {
        return flowScope.contains(Constants.FLOW_SURROGATE_EMAIL);
    }
}
