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

import fr.gouv.vitamui.cas.provider.Pac4jClientIdentityProviderDto;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.util.Optional;

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
        } else {
            return processLoginRequest(requestContext, flowScope);
        }
    }

    private Event processSubrogationRequest(RequestContext requestContext, MutableAttributeMap<Object> flowScope)
        throws IOException {
        // We came from subrogation validation
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

        if (ensureUserIsEnabled(superUserEmail, superUserCustomerId)) {
            return handleUserDisabled(superUserEmail, superUserCustomerId);
        }
        if (ensureUserIsEnabled(surrogateEmail, surrogateCustomerId)) {
            return handleUserDisabled(superUserEmail, surrogateCustomerId);
        }

        return dispatchUser(requestContext, superUserEmail, superUserCustomerId, surrogateEmail, surrogateCustomerId);
    }

    private Event processLoginRequest(RequestContext requestContext, MutableAttributeMap<Object> flowScope)
        throws IOException {
        String userEmail = (String) flowScope.get(Constants.FLOW_LOGIN_EMAIL);
        String customerId = (String) flowScope.get(Constants.FLOW_LOGIN_CUSTOMER_ID);

        LOGGER.debug("Login request of '{}' (customerId '{}')", userEmail, customerId);

        ParameterChecker.checkParameter("Missing authn params", userEmail, customerId);

        if (ensureUserIsEnabled(userEmail, customerId)) {
            return handleUserDisabled(userEmail, customerId);
        }

        return dispatchUser(requestContext, userEmail, customerId, null, null);
    }

    private Event dispatchUser(
        RequestContext requestContext,
        String loginEmail,
        String loginCustomerId,
        String surrogateEmail,
        String surrogateCustomerId
    ) throws IOException {
        Optional<IdentityProviderDto> providerOpt = identityProviderHelper.findByUserIdentifierAndCustomerId(
            providersService.getProviders(),
            loginEmail,
            loginCustomerId
        );
        if (providerOpt.isEmpty()) {
            LOGGER.error("No provider found for superUserCustomerId: {}", loginCustomerId);
            return new Event(this, BAD_CONFIGURATION);
        }
        var identityProviderDto = providerOpt.get();

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);

        if (identityProviderDto.getInternal()) {
            sessionStore.set(webContext, Constants.FLOW_LOGIN_EMAIL, null);
            sessionStore.set(webContext, Constants.FLOW_LOGIN_CUSTOMER_ID, null);
            sessionStore.set(webContext, Constants.FLOW_SURROGATE_EMAIL, null);
            sessionStore.set(webContext, Constants.FLOW_SURROGATE_CUSTOMER_ID, null);

            LOGGER.debug("Redirect the user to the password page...");
            return success();
        } else {
            LOGGER.debug(
                "Saving surrogate for after authentication delegation: loginEmail : {}, " +
                "loginCustomerId : {}, surrogateEmail : {}, surrogateCustomerId : {}",
                loginEmail,
                loginCustomerId,
                surrogateEmail,
                surrogateCustomerId
            );
            sessionStore.set(webContext, Constants.FLOW_LOGIN_EMAIL, loginEmail);
            sessionStore.set(webContext, Constants.FLOW_LOGIN_CUSTOMER_ID, loginCustomerId);
            sessionStore.set(webContext, Constants.FLOW_SURROGATE_EMAIL, surrogateEmail);
            sessionStore.set(webContext, Constants.FLOW_SURROGATE_CUSTOMER_ID, surrogateCustomerId);

            return utils.performClientRedirection(
                this,
                ((Pac4jClientIdentityProviderDto) identityProviderDto).getClient(),
                requestContext
            );
        }
    }

    private boolean ensureUserIsEnabled(String email, String customerId) {
        UserDto userDto =
            this.casExternalRestClient.getUserByEmailAndCustomerId(
                    utils.buildContext(email),
                    email,
                    customerId,
                    Optional.empty()
                );
        if (userDto == null) {
            // To avoid account existence disclosure, unknown users are silently ignored.
            // Once they enter their credentials, they will get a generic "login or password invalid" error message.
            return false;
        }
        return (userDto.getStatus() != UserStatusEnum.ENABLED);
    }

    private Event handleUserDisabled(final String emailUser, String customerId) {
        LOGGER.error("Bad status for user: {} ({})", emailUser, customerId);
        return new Event(this, DISABLED);
    }

    private static boolean isSubrogationMode(MutableAttributeMap<Object> flowScope) {
        return flowScope.contains(Constants.FLOW_SURROGATE_EMAIL);
    }
}
