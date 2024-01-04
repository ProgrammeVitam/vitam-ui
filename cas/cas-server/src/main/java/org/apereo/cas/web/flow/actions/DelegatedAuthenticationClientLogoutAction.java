package org.apereo.cas.web.flow.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
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
@Slf4j
@RequiredArgsConstructor
public class DelegatedAuthenticationClientLogoutAction extends BaseCasWebflowAction {
    protected final Clients clients;

    protected final SessionStore sessionStore;

    @Override
    protected Event doPreExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = new JEEContext(request, response);

        val currentProfile = findCurrentProfile(context);
        val clientResult = findCurrentClient(currentProfile);
        if (clientResult.isPresent()) {
            val client = clientResult.get();
            requestContext.getFlowScope().put("delegatedAuthenticationLogoutRequest", true);

            LOGGER.debug("Handling logout for delegated authentication client [{}]", client);
            WebUtils.putDelegatedAuthenticationClientName(requestContext, client.getName());
            sessionStore.set(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, client.getName());
        }
        return null;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = new JEEContext(request, response);

        val currentProfile = findCurrentProfile(context);
        val clientResult = findCurrentClient(currentProfile);
        if (clientResult.isPresent()) {
            val client = clientResult.get();
            LOGGER.trace("Located client [{}]", client);

            val service = WebUtils.getService(requestContext);
            val targetUrl = service != null ? service.getId() : null;
            LOGGER.debug("Logout target url based on service [{}] is [{}]", service, targetUrl);

            val actionResult = client.getLogoutAction(context, sessionStore, currentProfile, targetUrl);
            if (actionResult.isPresent()) {
                val action = (HttpAction) actionResult.get();
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
        val pm = new ProfileManager(webContext, this.sessionStore);
        val profile = pm.getProfile();
        return profile.orElse(null);
    }

    /**
     * Find the current client from the current profile.
     *
     * @param currentProfile the current profile
     * @return the current client
     */
    protected Optional<Client> findCurrentClient(final UserProfile currentProfile) {
        return currentProfile == null
            ? Optional.<Client>empty()
            : clients.findClient(currentProfile.getClientName());
    }
}

