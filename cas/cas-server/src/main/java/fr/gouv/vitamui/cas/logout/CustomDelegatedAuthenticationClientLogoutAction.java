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
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutConfirmationResolver;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.actions.logout.DelegatedAuthenticationClientLogoutAction;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
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

    // CAS 7.3 added the ticket registry, the CAS properties and the logout confirmation resolver to the parent
    // constructor, and hands a WebContext to findCurrentClient so providers can be resolved per request.
    public CustomDelegatedAuthenticationClientLogoutAction(
        final DelegatedIdentityProviders identityProviders,
        final SessionStore sessionStore,
        final TicketRegistry ticketRegistry,
        final CasConfigurationProperties casProperties,
        final LogoutConfirmationResolver logoutConfirmationResolver,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper
    ) {
        super(identityProviders, sessionStore, ticketRegistry, casProperties, logoutConfirmationResolver);
        this.providersService = providersService;
        this.identityProviderHelper = identityProviderHelper;
    }

    @Override
    protected Optional<? extends Client> findCurrentClient(
        final UserProfile currentProfile,
        final WebContext webContext
    ) {
        val optClient = currentProfile == null
            ? Optional.<Client>empty()
            : identityProviders.findClient(currentProfile.getClientName(), webContext);

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
