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
package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.model.CustomerModel;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.CustomerIdDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTED;
import static fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTION_VIEW;

/**
 * This class lists users matching provided login email:
 * - if subrogation mode : customerId is already provided in scope ==> continue to dispatcher
 * - if a single user is found ==> continue to dispatcher
 * - if multiple users found ==> redirect to customer selection page
 * - if no user found : act as if it exists (to avoid account existence disclosure)
 */
@RequiredArgsConstructor
public class ListCustomersAction extends AbstractAction {

    public static final String BAD_CONFIGURATION = "badConfiguration";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ListCustomersAction.class);

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    private final CasExternalRestClient casExternalRestClient;

    private final Utils utils;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws IOException {

        val flowScope = requestContext.getFlowScope();

        if (isSubrogationMode(flowScope)) {
            return processSubrogationRequest(flowScope);
        } else {
            return processEmailInput(requestContext, flowScope);
        }
    }

    private Event processSubrogationRequest(MutableAttributeMap<Object> flowScope)
        throws IOException {
        // We came from subrogation validation (emailForm)
        String surrogateEmail = (String) flowScope.get(Constants.FLOW_SURROGATE_EMAIL);
        String surrogateCustomerId = (String) flowScope.get(Constants.FLOW_SURROGATE_CUSTOMER_ID);
        String superUserEmail = (String) flowScope.get(Constants.FLOW_LOGIN_EMAIL);
        String superUserCustomerId = (String) flowScope.get(Constants.FLOW_LOGIN_CUSTOMER_ID);

        LOGGER.debug("Subrogation of '{}' (customerId '{}') by super admin '{}' (customerId '{}')",
            surrogateEmail, surrogateCustomerId, superUserEmail, superUserCustomerId);

        ParameterChecker.checkParameter("Missing subrogation params",
            surrogateEmail, surrogateCustomerId, superUserEmail, superUserCustomerId);

        // Filter by both email (domain) & customerId
        Optional<IdentityProviderDto> providerDto =
            identityProviderHelper.findByUserIdentifierAndCustomerId(providersService.getProviders(), superUserEmail,
                    superUserCustomerId);
        if (providerDto.isEmpty()) {
            LOGGER.error("No provider found for superUserEmail / superUserCustomerId: {}",
                superUserEmail, superUserCustomerId);
            return new Event(this, BAD_CONFIGURATION);
        }

        return handleSingleAuthenticationProvider(flowScope, superUserEmail, superUserCustomerId);
    }

    private Event processEmailInput(RequestContext requestContext, MutableAttributeMap<Object> flowScope) {

        UsernamePasswordCredential credential =
            WebUtils.getCredential(requestContext, UsernamePasswordCredential.class);
        String username = credential.getUsername().toLowerCase().trim();

        LOGGER.debug("User provided login of '{}'", username);

        List<UserDto> existingUsersList = getUsers(username);

        if (existingUsersList.size() == 1) {
            return processSingleUserForInputEmail(flowScope, username, existingUsersList.get(0));
        } else if (existingUsersList.isEmpty()) {
            // To avoid account existence disclosure, unknown users are silently ignored.
            // Once they enter their credentials, they will get a generic "login or password invalid" error message.
            return processNoUserFoundMatchingInputEmail(flowScope, username);
        } else {
            return processMultipleUsersForInputEmail(flowScope, username, existingUsersList);
        }
    }

    private Event processSingleUserForInputEmail(MutableAttributeMap<Object> flowScope, String username, UserDto user) {

        // Ensure user has a proper Identity Provided configured, and redirect to dispatcher...
        LOGGER.debug("A single user matched provided login of '{}': {}", username, user);

        String customerId = user.getCustomerId();
        Optional<IdentityProviderDto> provider = identityProviderHelper.
            findByUserIdentifierAndCustomerId(providersService.getProviders(), username, customerId);
        if (provider.isEmpty()) {
            LOGGER.error("No provider found for customerId: {}", customerId);
            return new Event(this, BAD_CONFIGURATION);
        }

        return handleSingleAuthenticationProvider(flowScope, username, customerId);
    }

    @NotNull
    private Event processNoUserFoundMatchingInputEmail(MutableAttributeMap<Object> flowScope, String username) {

        List<IdentityProviderDto> identityProviders =
            identityProviderHelper.findAllByUserIdentifier(providersService.getProviders(), username);

        if (identityProviders.isEmpty()) {
            LOGGER.warn("No provider found for email: '{}'", username);
            return new Event(this, BAD_CONFIGURATION);
        }

        if (identityProviders.size() == 1) {
            LOGGER.debug("User {} not found in DB. To avoid account existence disclosure, we'll just redirect" +
                " to provider login page.", username);
            // User not found, but email domain matches existing provider
            return handleSingleAuthenticationProvider(flowScope, username, identityProviders.get(0).getCustomerId());
        }

        List<String> availableCustomerIds = identityProviders.stream()
            .map(CustomerIdDto::getCustomerId)
            .collect(Collectors.toList());

        LOGGER.debug("User '{}' not found in DB. To avoid account existence disclosure, we'll just redirect" +
            " to customer selection page. Available customerIds: {}", username, availableCustomerIds);

        return handleMultipleAuthenticationProviders(flowScope, username, availableCustomerIds);
    }

    @NotNull
    private Event processMultipleUsersForInputEmail(MutableAttributeMap<Object> flowScope, String username,
        List<UserDto> existingUsersList) {
        LOGGER.debug("Multiple users found for '{}'. Show customer selection page", username);

        // Multiple users found ==> Redirect user to customerId selection page
        List<String> availableCustomerIds =
            existingUsersList.stream().map(UserDto::getCustomerId).collect(Collectors.toList());

        return handleMultipleAuthenticationProviders(flowScope, username, availableCustomerIds);
    }

    private Event handleSingleAuthenticationProvider(MutableAttributeMap<Object> flowScope,
        String loginEmail, String customerId) {
        LOGGER.debug("User '{}' has a single available customer {}. No need for customer selection page",
            loginEmail, customerId);

        flowScope.put(Constants.FLOW_LOGIN_EMAIL, loginEmail);
        flowScope.put(Constants.FLOW_LOGIN_CUSTOMER_ID, customerId);
        flowScope.remove(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST);

        return new Event(this, TRANSITION_TO_CUSTOMER_SELECTED);
    }

    private Event handleMultipleAuthenticationProviders(MutableAttributeMap<Object> flowScope, String username,
        List<String> availableCustomerIds) {

        LOGGER.debug("Redirecting user with login of '{}' to customer selection page. Available customerIds: {}",
            username, availableCustomerIds);

        List<CustomerDto> customers = casExternalRestClient.getCustomersByIds(utils.buildContext(username),
            availableCustomerIds);

        LOGGER.debug("Available customers: {}", customers);

        List<CustomerModel> customerToSelect = customers.stream().map(
                customerDto -> new CustomerModel()
                    .setCustomerId(customerDto.getId())
                    .setCode(customerDto.getCode())
                    .setName(customerDto.getName())
            )
            .sorted(Comparator.comparing(CustomerModel::getCode))
            .collect(Collectors.toList());

        flowScope.put(Constants.FLOW_LOGIN_EMAIL, username);
        flowScope.remove(Constants.FLOW_LOGIN_CUSTOMER_ID);
        flowScope.put(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST, customerToSelect);

        return new Event(this, TRANSITION_TO_CUSTOMER_SELECTION_VIEW);
    }

    private List<UserDto> getUsers(String email) {
            return casExternalRestClient.getUsersByEmail(utils.buildContext(email),
                email, Optional.empty());
    }

    private static boolean isSubrogationMode(MutableAttributeMap<Object> flowScope) {
        return flowScope.contains(Constants.FLOW_SURROGATE_EMAIL);
    }
}
