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
package fr.gouv.vitamui.cas.ticket;

import fr.gouv.vitamui.commons.api.CommonConstants;
import lombok.Getter;
import lombok.val;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.*;
import org.apereo.cas.token.JwtBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Specific factory for access tokens using the auth token as identifier.
 *
 *
 */
@Getter
public class CustomOAuth20DefaultAccessTokenFactory extends OAuth20DefaultAccessTokenFactory {

    public CustomOAuth20DefaultAccessTokenFactory(
        final ExpirationPolicyBuilder<OAuth20AccessToken> expirationPolicy,
        final JwtBuilder jwtBuilder,
        final ServicesManager servicesManager
    ) {
        super(expirationPolicy, jwtBuilder, servicesManager);
    }

    @Override
    public OAuth20AccessToken create(
        final Service service,
        final Authentication authentication,
        final TicketGrantingTicket ticketGrantingTicket,
        final Collection<String> scopes,
        final String token,
        final String clientId,
        final Map<String, Map<String, Object>> requestClaims,
        final OAuth20ResponseTypes responseType,
        final OAuth20GrantTypes grantType
    ) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            jwtBuilder.getServicesManager(),
            clientId
        );
        val expirationPolicyToUse = determineExpirationPolicyForService(registeredService);
        // CUSTO: don't generate the identifier, but use the token of the principal
        val accessTokenId = generateAccessTokenId(authentication);

        val at = new OAuth20DefaultAccessToken(
            accessTokenId,
            service,
            authentication,
            expirationPolicyToUse,
            ticketGrantingTicket,
            token,
            scopes,
            clientId,
            requestClaims,
            responseType,
            grantType
        );
        if (ticketGrantingTicket != null) {
            ticketGrantingTicket.getDescendantTickets().add(at.getId());
        }
        return at;
    }

    private String generateAccessTokenId(final Authentication authentication) {
        final Principal principal = authentication.getPrincipal();
        final List<Object> authToken = principal.getAttributes().get(CommonConstants.AUTHTOKEN_ATTRIBUTE);
        if (authToken == null || authToken.size() == 0) {
            throw new RuntimeException("Cannot create access token for null authtoken: " + principal);
        }
        return (String) authToken.get(0);
    }
}
