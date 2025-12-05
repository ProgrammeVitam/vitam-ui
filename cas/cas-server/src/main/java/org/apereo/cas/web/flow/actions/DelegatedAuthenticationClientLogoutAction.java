package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * To be removed when upgrading to CAS version >= 6.6.13
 */

public class DelegatedAuthenticationClientLogoutAction extends BaseCasWebflowAction {

    protected final Clients clients;

    protected final SessionStore sessionStore;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        DelegatedAuthenticationClientLogoutAction.class
    );

    public DelegatedAuthenticationClientLogoutAction(final Clients clients, final SessionStore sessionStore) {
        this.clients = clients;
        this.sessionStore = sessionStore;
    }

    @Override
    protected Event doPreExecute(final RequestContext requestContext) {
        var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        var context = new JEEContext(request, response);

        var currentProfile = findCurrentProfile(context);
        var clientResult = findCurrentClient(currentProfile);
        if (clientResult.isPresent()) {
            var client = clientResult.get();
            requestContext.getFlowScope().put("delegatedAuthenticationLogoutRequest", true);

            LOGGER.debug("Handling logout for delegated authentication client [{}]", client);
            // WebUtils.putDelegatedAuthenticationClientName(requestContext,
            // client.getName());
            sessionStore.set(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, client.getName());
        }
        return null;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        var context = new JEEContext(request, response);

        var currentProfile = findCurrentProfile(context);
        var clientResult = findCurrentClient(currentProfile);
        if (clientResult.isPresent()) {
            var client = clientResult.get();
            LOGGER.trace("Located client [{}]", client);

            var service = WebUtils.getService(requestContext);
            var targetUrl = service != null ? service.getId() : null;
            LOGGER.debug("Logout target url based on service [{}] is [{}]", service, targetUrl);

            var callContext = new CallContext(context, sessionStore);
            var actionResult = client.getLogoutAction(callContext, currentProfile, targetUrl);
            if (actionResult.isPresent()) {
                var action = (HttpAction) actionResult.get();
                LOGGER.debug("Adapting logout action [{}] for client [{}]", action, client);
                JEEHttpActionAdapter.INSTANCE.adapt(action, context);
            }
        } else {
            LOGGER.debug("The current client cannot be found; No logout action can execute");
        }
        return null;
    }

    /**
     * Finds the current profile from the context.
     *
     * @param webContext A web context (request + response).
     * @return The common profile active.
     */
    protected UserProfile findCurrentProfile(final JEEContext webContext) {
        var pm = new ProfileManager(webContext, this.sessionStore);
        var profile = pm.getProfile();
        return profile.orElse(null);
    }

    /**
     * Find the current client from the current profile.
     *
     * @param currentProfile the current profile
     * @return the current client
     */
    protected Optional<Client> findCurrentClient(final UserProfile currentProfile) {
        return currentProfile == null ? Optional.<Client>empty() : clients.findClient(currentProfile.getClientName());
    }
}
