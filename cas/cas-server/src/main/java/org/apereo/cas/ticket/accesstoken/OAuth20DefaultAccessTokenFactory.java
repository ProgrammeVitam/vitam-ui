package org.apereo.cas.ticket.accesstoken;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.*;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import java.util.Collection;
import java.util.Map;

/**
 * To be removed when upgrading to CAS version >= 6.6.3.
 */
@RequiredArgsConstructor
@Getter
public class OAuth20DefaultAccessTokenFactory implements OAuth20AccessTokenFactory {

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator accessTokenIdGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicyBuilder<OAuth20AccessToken> expirationPolicy;

    /**
     * JWT builder instance.
     */
    protected final JwtBuilder jwtBuilder;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    public OAuth20DefaultAccessTokenFactory(
        final ExpirationPolicyBuilder<OAuth20AccessToken> expirationPolicy,
        final JwtBuilder jwtBuilder,
        final ServicesManager servicesManager
    ) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy, jwtBuilder, servicesManager);
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
        // CUSTO:
        val accessTokenId = generateAccessTokenId(service, authentication);

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

    // CUSTO:
    protected String generateAccessTokenId(final Service service, final Authentication authentication) {
        return this.accessTokenIdGenerator.getNewTicketId(OAuth20AccessToken.PREFIX);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OAuth20AccessToken.class;
    }

    /**
     * Determine the expiration policy for the registered service.
     *
     * @param registeredService the registered service
     * @return the expiration policy
     */
    protected ExpirationPolicy determineExpirationPolicyForService(final OAuthRegisteredService registeredService) {
        if (registeredService != null && registeredService.getAccessTokenExpirationPolicy() != null) {
            val policy = registeredService.getAccessTokenExpirationPolicy();
            val maxTime = policy.getMaxTimeToLive();
            val ttl = policy.getTimeToKill();
            if (StringUtils.isNotBlank(maxTime) && StringUtils.isNotBlank(ttl)) {
                return new OAuth20AccessTokenExpirationPolicy(
                    Beans.newDuration(maxTime).getSeconds(),
                    Beans.newDuration(ttl).getSeconds()
                );
            }
        }
        return this.expirationPolicy.buildTicketExpirationPolicy();
    }
}
