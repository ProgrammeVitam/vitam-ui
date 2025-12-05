package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import org.apereo.cas.web.flow.actions.DelegatedAuthenticationClientLogoutAction;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.UserProfile;

import java.util.Optional;

/**
 * Propagate the logout from CAS to the authn delegated server.
 */

public class CustomDelegatedAuthenticationClientLogoutAction extends DelegatedAuthenticationClientLogoutAction {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        CustomDelegatedAuthenticationClientLogoutAction.class
    );

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    public CustomDelegatedAuthenticationClientLogoutAction(
        final Clients clients,
        final SessionStore sessionStore,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper
    ) {
        super(clients, sessionStore);
        this.providersService = providersService;
        this.identityProviderHelper = identityProviderHelper;
    }

    @Override
    protected Optional<Client> findCurrentClient(final UserProfile currentProfile) {
        final var optClient = currentProfile == null
            ? Optional.<Client>empty()
            : clients.findClient(currentProfile.getClientName());

        LOGGER.debug("optClient: {}", optClient);
        if (optClient.isEmpty()) {
            return Optional.empty();
        }

        var client = optClient.get();
        var provider = identityProviderHelper
            .findByTechnicalName(providersService.getProviders(), client.getName())
            .get();
        LOGGER.debug("provider: {}", provider);
        if (!provider.isPropagateLogout()) {
            return Optional.empty();
        }

        return optClient;
    }
}
