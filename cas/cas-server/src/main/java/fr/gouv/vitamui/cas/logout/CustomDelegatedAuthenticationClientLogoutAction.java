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

package fr.gouv.vitamui.cas.logout;

import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.actions.logout.DelegatedAuthenticationClientLogoutAction;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * Propagate the logout from CAS to the authn delegated server.
 */
@Slf4j
public class CustomDelegatedAuthenticationClientLogoutAction extends DelegatedAuthenticationClientLogoutAction {

    /** Flow scope attribute carrying the provider logout url, read by {@link CustomFinishLogoutAction}. */
    public static final String PROVIDER_LOGOUT_URL_ATTRIBUTE = "vitamuiProviderLogoutUrl";

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    public CustomDelegatedAuthenticationClientLogoutAction(
        final DelegatedIdentityProviders identityProviders,
        final SessionStore sessionStore,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper
    ) {
        super(identityProviders, sessionStore);
        this.providersService = providersService;
        this.identityProviderHelper = identityProviderHelper;
    }

    /**
     * Records the provider logout url so that the end of the flow can redirect to it.
     * <p>
     * The parent action asks pac4j for the provider logout action and writes it straight to the
     * response as a {@code Location} header. That is enough when the browser reaches
     * {@code /cas/logout} directly, but not when it goes through the OIDC end-session endpoint:
     * {@code OidcLogoutEndpointController} stores the {@code post_logout_redirect_uri} before
     * forwarding here, and `FinishLogoutAction` ends the flow on it instead — leaving the provider
     * session open.
     * <p>
     * The url cannot be promoted here, because `LogoutAction` runs afterwards and overwrites
     * `logoutRedirectUrl` with the service url whenever `cas.logout.follow-service-redirects` is on.
     * It is therefore parked in the flow scope, and {@link CustomFinishLogoutAction} promotes it
     * once every other action has had its say.
     */
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        final var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        final Event event = super.doExecuteInternal(requestContext);

        // SAML2 needs none of this and breaks with it: DelegatedAuthenticationClientFinishLogoutAction
        // hands the flow redirect url to SAML2LogoutProcessor.setPostLogoutURL() and then clears it, so
        // that the SAML binding drives the return once the IdP answers with its LogoutResponse. Putting
        // a redirect url back would leave the browser on a dead end instead.
        if (isSaml2Client(requestContext)) {
            LOGGER.debug("SAML2 logout is driven by CAS itself, leaving the flow redirect url alone");
            return event;
        }

        final String providerLogoutUrl = response.getHeader(HttpConstants.LOCATION_HEADER);
        if (StringUtils.isBlank(providerLogoutUrl)) {
            LOGGER.debug("No logout action was produced for the delegated provider, nothing to propagate");
            return event;
        }

        LOGGER.debug("Delegated provider logout url to redirect to at the end of the flow: {}", providerLogoutUrl);
        requestContext.getFlowScope().put(PROVIDER_LOGOUT_URL_ATTRIBUTE, providerLogoutUrl);
        return event;
    }

    private boolean isSaml2Client(final RequestContext requestContext) {
        final String clientName = DelegationWebflowUtils.getDelegatedAuthenticationClientName(requestContext);
        return (
            StringUtils.isNotBlank(clientName) &&
            identityProviders.findClient(clientName).filter(SAML2Client.class::isInstance).isPresent()
        );
    }

    @Override
    protected Optional<Client> findCurrentClient(final UserProfile currentProfile) {
        val optClient = currentProfile == null
            ? Optional.<Client>empty()
            : identityProviders.findClient(currentProfile.getClientName());

        LOGGER.debug("optClient: {}", optClient);
        if (optClient.isEmpty()) {
            return Optional.empty();
        }

        val client = optClient.get();
        val provider = identityProviderHelper
            .findByTechnicalName(providersService.getProviders(), client.getName())
            .get();
        LOGGER.debug("provider: {}", provider);
        if (!provider.isPropagateLogout()) {
            return Optional.empty();
        }

        return optClient;
    }
}
