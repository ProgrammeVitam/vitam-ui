package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.web.flow.actions.DelegatedAuthenticationClientLogoutAction;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.UserProfile;

import java.util.Optional;

/**
 * Propagate the logout from CAS to the authn delegated server.
 */
@Slf4j
public class CustomDelegatedAuthenticationClientLogoutAction extends DelegatedAuthenticationClientLogoutAction {

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    public CustomDelegatedAuthenticationClientLogoutAction(final Clients clients, final SessionStore sessionStore,
                                                           final ProvidersService providersService,
                                                           final IdentityProviderHelper identityProviderHelper) {
        super(clients, sessionStore);
        this.providersService = providersService;
        this.identityProviderHelper = identityProviderHelper;
    }

    @Override
    protected Optional<Client> findCurrentClient(final UserProfile currentProfile) {
        val optClient = currentProfile == null
            ? Optional.<Client>empty()
            : clients.findClient(currentProfile.getClientName());

        LOGGER.debug("optClient: {}", optClient);
        if (optClient.isEmpty()) {
            return Optional.empty();
        }

        val client = optClient.get();
        val provider = identityProviderHelper.findByTechnicalName(providersService.getProviders(), client.getName()).get();
        LOGGER.debug("provider: {}", provider);
        if (!provider.isPropagateLogout()) {
            return Optional.empty();
        }

        return optClient;
    }
}
