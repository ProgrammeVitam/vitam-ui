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
import fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class can dispatch the user:
 * - either to the password page
 * - or to an external IdP (authentication delegation)
 * - or to the bad configuration page if the user is not linked to any identity provider
 * - or to the disabled account page if the user is disabled.
 */
@RequiredArgsConstructor
public class DispatcherAction extends AbstractAction {

    public static final String DISABLED = "disabled";
    public static final String BAD_CONFIGURATION = "badConfiguration";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(DispatcherAction.class);

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    private final CasExternalRestClient casExternalRestClient;

    private final String surrogationSeparator;

    private final Utils utils;

    private final SessionStore sessionStore;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws IOException {

        val credential = WebUtils.getCredential(requestContext, UsernamePasswordCredential.class);
        val username = credential.getUsername().toLowerCase().trim();
        String dispatchedUsername = username;
        String surrogate = null;
        if (username.contains(surrogationSeparator)) {
            dispatchedUsername = StringUtils.substringAfter(username, surrogationSeparator).trim();
            if (username.startsWith(surrogationSeparator)) {
                WebUtils.putCredential(requestContext, new UsernamePasswordCredential(dispatchedUsername, null));
            } else {
                surrogate = StringUtils.substringBefore(username, surrogationSeparator).trim();
            }
        }
        LOGGER.debug("Dispatched user: {} / surrogate: {}", dispatchedUsername, surrogate);

        List<UserDto> dispatchedUsers;
        try {
            dispatchedUsers = casExternalRestClient.getUsersByEmail(utils.buildContext(dispatchedUsername), dispatchedUsername, Optional.empty());
            if (dispatchedUsers.stream().allMatch(UserDto::notEnabled)) {
                return userDisabledEvent(dispatchedUsername);
            }
        } catch (final InvalidFormatException e) {
            return userDisabledEvent(dispatchedUsername);
        }
        dispatchedUsers = dispatchedUsers.stream().filter(UserDto::enabled).collect(Collectors.toList());
        if (surrogate != null) {
            try {
                List<UserDto> surrogateUsers = casExternalRestClient.getUsersByEmail(utils.buildContext(surrogate), surrogate, Optional.empty());
                if (surrogateUsers.stream().allMatch(UserDto::notEnabled)) {
                    return userDisabledEvent(surrogate);
                }
            } catch (final InvalidFormatException e) {
                return userDisabledEvent(surrogate);
            }
        }

        if (dispatchedUsers.size() > 1) {
            // return all organisation
            LOGGER.debug("Redirect the user to the selection customer page");
            requestContext.getRequestScope().put(CustomLoginWebflowConfigurer.STATE_BINDING_CUSTOMER_MODEL, new CustomerModel());

            return result(CasWebflowConstants.TRANSITION_ID_APPROVE, "customers", List.of(
                new CustomerDto().setName("POUET-01").setCode("code-pouet-01"),
                new CustomerDto().setName("POUET-02").setCode("code-pouet-02"),
                new CustomerDto().setName("POUET-03").setCode("code-pouet-03"),
                new CustomerDto().setName("POUET-04").setCode("code-pouet-04")
            ));
        }

        //SELECTION ORGA (List<Pac4jClientIdentityProviderDto>)
        val providers = identityProviderHelper.findByUserIdentifier(providersService.getProviders(), dispatchedUsername);
        if (providers.isEmpty()) {
            return new Event(this, BAD_CONFIGURATION);
        }
        if (providers.size() > 1) {
            throw new NotImplementedException("Multiple providers found for user.");
        }

        Pac4jClientIdentityProviderDto provider = (Pac4jClientIdentityProviderDto) providers.get(0);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);


        if (provider.getInternal()) {
            sessionStore.set(webContext, Constants.SURROGATE, null);
            LOGGER.debug("Redirect the user to the password page");
            return success();
        } else {
            // save the surrogate in the session to be retrieved by the UserPrincipalResolver and DelegatedSurrogateAuthenticationPostProcessor
            if (surrogate != null) {
                LOGGER.debug("Saving surrogate for after authentication delegation: {}", surrogate);
                sessionStore.set(webContext, Constants.SURROGATE, surrogate);
            }
            return utils.performClientRedirection(this, provider.getClient(), requestContext);
        }
    }

    private Event userDisabledEvent(final String emailUser) {
        LOGGER.error("Bad status for user: {}", emailUser);
        return new Event(this, DISABLED);
    }

}
