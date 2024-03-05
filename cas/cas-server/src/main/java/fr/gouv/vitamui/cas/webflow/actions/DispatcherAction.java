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
import fr.gouv.vitamui.cas.provider.Pac4jClientIdentityProviderDto;
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
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
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

/**
 * This class can dispatch the user:
 * - either to customer selection page (if user have multiple accounts for different customers)
 * - or to the password page
 * - or to an external IdP (authentication delegation)
 * - or to the bad configuration page if the user is not linked to any identity provider
 * - or to the disabled account page if the user is disabled.
 */
@RequiredArgsConstructor
public class DispatcherAction extends AbstractAction {

    public static final String DISABLED = "disabled";
    public static final String BAD_CONFIGURATION = "badConfiguration";
    public static final String TRANSITION_SELECT_CUSTOMER = "selectCustomer";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(DispatcherAction.class);

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    private final CasExternalRestClient casExternalRestClient;

    private final Utils utils;

    private final SessionStore sessionStore;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws IOException {

        val flowScope = requestContext.getFlowScope();

        if (isSubrogationMode(flowScope)) {
            return processSubrogationRequest(requestContext, flowScope);
        } else if (isCustomerSelectionMode(flowScope)) {
            return processCustomerSelection(requestContext, flowScope);
        } else {
            return processEmailInput(requestContext, flowScope);
        }
    }

    private Event processEmailInput(RequestContext requestContext, MutableAttributeMap<Object> flowScope)
        throws IOException {

        // Nominal case: We came from user login view (emailForm)
        UsernamePasswordCredential credential =
            WebUtils.getCredential(requestContext, UsernamePasswordCredential.class);
        String username = credential.getUsername().toLowerCase().trim();

        LOGGER.debug("User provided login of '{}'", username);

        List<UserDto> existingUsersList = getUsers(username);

        if (existingUsersList.size() == 1) {
            // Nominal case: A single user found ==> Ensure it's not disabled & persist its customerId

            UserDto user = existingUsersList.get(0);
            LOGGER.debug("A single user matched provided login of '{}': {}", username, user);

            if (isUserDisabled(user)) {
                return handleUserDisabled(username);
            }

            String customerId = user.getCustomerId();
            Optional<IdentityProviderDto> provider =
                identityProviderHelper.findByCustomerId(providersService.getProviders(), customerId);
            if (provider.isEmpty()) {
                LOGGER.error("No provider found for customerId: {}", customerId);
                return new Event(this, BAD_CONFIGURATION);
            }

            return handleSingleAuthenticationProvider(requestContext, username, customerId, provider.get());

        } else if (existingUsersList.isEmpty()) {

            // Not found!

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
                return handleSingleAuthenticationProvider(requestContext, username,
                    identityProviders.get(0).getCustomerId(), identityProviders.get(0));
            }

            List<String> availableCustomerIds = identityProviders.stream()
                .map(CustomerIdDto::getCustomerId)
                .collect(Collectors.toList());

            LOGGER.debug("User '{}' not found in DB. To avoid account existence disclosure, we'll just redirect" +
                " to customer selection page. Available customerIds: {}", username, availableCustomerIds);

            return handleMultipleAuthenticationProviders(flowScope, username, availableCustomerIds);

        } else {

            LOGGER.debug("Multiple users found for '{}'. Show customer selection page", username);

            // Multiple users found ==> Redirect user to customerId selection page
            List<String> availableCustomerIds =
                existingUsersList.stream().map(UserDto::getCustomerId).collect(Collectors.toList());

            return handleMultipleAuthenticationProviders(flowScope, username, availableCustomerIds);
        }
    }

    private Event processCustomerSelection(RequestContext requestContext, MutableAttributeMap<Object> flowScope)
        throws IOException {
        // We came from customer selection view (customerForm)
        String loginEmail = flowScope.getRequiredString(Constants.FLOW_LOGIN_EMAIL);
        String customerId = requestContext.getRequestParameters().get(Constants.SELECT_CUSTOMER_ID_PARAM);

        List<CustomerModel> customerModels =
            (List<CustomerModel>) flowScope.getRequired(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST);

        CustomerModel customerModel = customerModels.stream()
            .filter(c -> c.getCustomerId().equals(customerId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid customerId '" + customerId + "'"));

        LOGGER.debug("Valid customer selected: {}", customerModel);

        Optional<IdentityProviderDto> provider =
            identityProviderHelper.findByCustomerId(providersService.getProviders(), customerId);
        if (provider.isEmpty()) {
            LOGGER.error("No provider found for customerId: {}", customerId);
            return new Event(this, BAD_CONFIGURATION);
        }

        return handleSingleAuthenticationProvider(requestContext, loginEmail, customerId, provider.get());
    }

    private Event processSubrogationRequest(RequestContext requestContext, MutableAttributeMap<Object> flowScope)
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

        // Ensure users are not disabled
        Optional<UserDto> surrogateUserDto = getUser(surrogateEmail, surrogateCustomerId);
        if (surrogateUserDto.isPresent() && isUserDisabled(surrogateUserDto.get())) {
            return handleUserDisabled(surrogateEmail);
        }

        Optional<UserDto> superUserUserDto = getUser(superUserEmail, superUserCustomerId);
        if (superUserUserDto.isPresent() && isUserDisabled(superUserUserDto.get())) {
            return handleUserDisabled(superUserEmail);
        }

        Optional<IdentityProviderDto> provider =
            identityProviderHelper.findByCustomerId(providersService.getProviders(), superUserCustomerId);
        if (provider.isEmpty()) {
            LOGGER.error("No provider found for superUserCustomerId: {}", superUserCustomerId);
            return new Event(this, BAD_CONFIGURATION);
        }

        return handleSingleAuthenticationProvider(requestContext, superUserEmail, superUserCustomerId,
            provider.get());
    }

    private Event handleSingleAuthenticationProvider(RequestContext requestContext, String username,
        String customerId, IdentityProviderDto identityProviderDto) throws IOException {

        val flowScope = requestContext.getFlowScope();
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);

        if (identityProviderDto.getInternal()) {

            flowScope.put(Constants.FLOW_LOGIN_EMAIL, username);
            flowScope.put(Constants.FLOW_LOGIN_CUSTOMER_ID, customerId);

            // FIXME :
            //  sessionStore.set(webContext, Constants.SURROGATE, null);
            LOGGER.debug("Redirect the user to the password page...");
            return success();

        } else {

            // FIXME :
            // // save the surrogate in the session to be retrieved by the UserPrincipalResolver and DelegatedSurrogateAuthenticationPostProcessor
            //  if (surrogate != null) {
            //      LOGGER.debug("Saving surrogate for after authentication delegation: {}", surrogate);
            //      sessionStore.set(webContext, Constants.SURROGATE, surrogate);
            // }

            return utils.performClientRedirection(this,
                ((Pac4jClientIdentityProviderDto) identityProviderDto).getClient(), requestContext);
        }
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
                    .setName(customerDto.getCompanyName())
            )
            .sorted(Comparator.comparing(CustomerModel::getCode))
            .collect(Collectors.toList());

        flowScope.put(Constants.FLOW_LOGIN_EMAIL, username);
        flowScope.remove(Constants.FLOW_LOGIN_CUSTOMER_ID);
        flowScope.put(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST, customerToSelect);

        return new Event(this, TRANSITION_SELECT_CUSTOMER);
    }

    private Event handleUserDisabled(final String emailUser) {
        LOGGER.error("Bad status for user: {}", emailUser);
        return new Event(this, DISABLED);
    }

    private Optional<UserDto> getUser(String email, String customerId) {
        return getUsers(email).stream()
            .filter(user -> user.getCustomerId().equals(customerId))
            .findFirst();
    }

    private List<UserDto> getUsers(String email) {
        try {
            return casExternalRestClient.getUsersByEmail(utils.buildContext(email),
                email, Optional.empty());
        } catch (final NotFoundException ignored) {
            // To avoid account existence disclosure, unknown users are silently ignored.
            // Once they enter their credentials, they will get a generic "login or password invalid" error message.
            return Collections.emptyList();
        }
    }

    private static boolean isUserDisabled(UserDto dispatcherUserDto) {
        return (dispatcherUserDto != null && dispatcherUserDto.getStatus() != UserStatusEnum.ENABLED);
    }

    private static boolean isSubrogationMode(MutableAttributeMap<Object> flowScope) {
        return flowScope.contains(Constants.FLOW_SURROGATE_EMAIL);
    }

    private static boolean isCustomerSelectionMode(MutableAttributeMap<Object> flowScope) {
        return flowScope.contains(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST);
    }
}
