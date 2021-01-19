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

import fr.gouv.vitamui.cas.provider.SamlIdentityProviderDto;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.util.Optional;

import lombok.val;

/**
 * This class can dispatch the user:
 * - either to the password page
 * - or to an external IdP (authentication delegation)
 * - or to the bad configuration page if the user is not linked to any identity provider
 * - or to the disabled account page if the user is disabled.
 *
 *
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
        String dispatchedUser = username;
        String surrogate = null;
        if (username.contains(surrogationSeparator)) {
            dispatchedUser = StringUtils.substringAfter(username, surrogationSeparator).trim();
            if (username.startsWith(surrogationSeparator)) {
                WebUtils.putCredential(requestContext, new UsernamePasswordCredential(dispatchedUser, null));
            } else {
                surrogate = StringUtils.substringBefore(username, surrogationSeparator).trim();
            }
        }
        LOGGER.debug("Dispatching user: {} / surrogate: {}", dispatchedUser, surrogate);

        // if the user is disabled, send him to a specific page (ignore not found users: it will fail when checking login/password)
        try {
            val dispatcherUserDto = casExternalRestClient.getUserByEmail(utils.buildContext(dispatchedUser), dispatchedUser, Optional.empty());
            if (dispatcherUserDto != null && dispatcherUserDto.getStatus() != UserStatusEnum.ENABLED) {
                return userDisabled(dispatchedUser);
            }
        } catch (final InvalidFormatException e) {
            return userDisabled(dispatchedUser);
        } catch (final NotFoundException e) {
        }
        if (surrogate != null) {
            try {
                val surrogateDto = casExternalRestClient.getUserByEmail(utils.buildContext(surrogate), surrogate, Optional.empty());
                if (surrogateDto != null && surrogateDto.getStatus() != UserStatusEnum.ENABLED) {
                    LOGGER.error("Bad status for surrogate: {}", surrogate);
                    return userDisabled(surrogate);
                }
            } catch (final InvalidFormatException e) {
                return userDisabled(surrogate);
            } catch (final NotFoundException e) {
            }
        }

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        boolean isInternal;
        val provider = (SamlIdentityProviderDto) identityProviderHelper.findByUserIdentifier(providersService.getProviders(), dispatchedUser).orElse(null);
        if (provider != null) {
            isInternal = provider.getInternal();
        } else {
            return new Event(this, BAD_CONFIGURATION);
        }
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response, sessionStore);
        sessionStore.set(webContext, Constants.PROVIDER_TECHNICAL_NAME, provider.getTechnicalName());
        if (isInternal) {
            sessionStore.set(webContext, Constants.SURROGATE, null);
            LOGGER.debug("Redirect the user to the password page...");
            return success();
        } else {

            // save the surrogate in the session to be retrieved by the UserPrincipalResolver and DelegatedSurrogateAuthenticationPostProcessor
            if (surrogate != null) {
                LOGGER.debug("Saving surrogate for after authentication delegation: {}", surrogate);
                sessionStore.set(webContext, Constants.SURROGATE, surrogate);
            }

            return utils.performClientRedirection(this, provider.getSaml2Client(), requestContext);
        }
    }

    private Event userDisabled(final String emailUser) {
        LOGGER.error("Bad status for user: {}", emailUser);
        return new Event(this, DISABLED);
    }
}
