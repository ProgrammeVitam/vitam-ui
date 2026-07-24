/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Dynamic {@link ClientRegistrationRepository} that fetches OIDC identity providers from IAM on demand
 * — a single {@link IdentityProviderDto} per {@code registrationId} (which is the Mongo id of the IdP).
 *
 * <p>The IAM lookup is expensive (network call + secrets in the response), so results are cached in
 * Caffeine with a 60 s TTL. That's the same cadence pac4j's {@code ProvidersService} uses on the CAS
 * side and is a good compromise between config freshness and load on IAM.
 *
 * <p>{@code null} is returned when the id doesn't correspond to an OIDC provider — Spring's OAuth2
 * login filter treats it as "unknown registration".
 */
@Component
public class MongoClientRegistrationRepository implements ClientRegistrationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientRegistrationRepository.class);
    private static final String OIDC = "OIDC";

    private final IamClient iamClient;
    private final Cache<String, Optional<ClientRegistration>> cache;

    public MongoClientRegistrationRepository(IamClient iamClient) {
        this.iamClient = iamClient;
        this.cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(100).build();
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return cache.get(registrationId, this::load).orElse(null);
    }

    private Optional<ClientRegistration> load(String registrationId) {
        IdentityProviderDto idp;
        try {
            idp = iamClient.getIdentityProvider(registrationId);
        } catch (IamClient.IdentityProviderNotFoundException e) {
            LOGGER.debug("IdP {} not found in IAM", registrationId);
            return Optional.empty();
        }
        if (idp == null) {
            return Optional.empty();
        }
        if (!OIDC.equalsIgnoreCase(idp.getProtocoleType())) {
            LOGGER.debug("IdP {} is not an OIDC provider (protocoleType={})", registrationId, idp.getProtocoleType());
            return Optional.empty();
        }
        if (!StringUtils.hasText(idp.getDiscoveryUrl()) || !StringUtils.hasText(idp.getClientId())) {
            LOGGER.warn("IdP {} misses discoveryUrl or clientId — skipping", registrationId);
            return Optional.empty();
        }

        String[] scopes = (StringUtils.hasText(idp.getScope()) ? idp.getScope() : "openid").split("\\s+");

        ClientRegistration registration = ClientRegistrations
            // fromIssuerLocation() also accepts a full discovery URL (Spring probes both endpoints).
            .fromIssuerLocation(stripWellKnown(idp.getDiscoveryUrl()))
            .registrationId(idp.getId())
            .clientId(idp.getClientId())
            .clientSecret(idp.getClientSecret())
            .scope(scopes)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .clientName(idp.getName())
            .build();
        LOGGER.info(
            "Loaded OIDC ClientRegistration for IdP {} (name={}, issuer={})",
            registrationId,
            idp.getName(),
            registration.getProviderDetails().getIssuerUri()
        );
        return Optional.of(registration);
    }

    /** Accept either the issuer or the full discovery URL — strip {@code /.well-known/openid-configuration}. */
    private static String stripWellKnown(String url) {
        int idx = url.indexOf("/.well-known");
        return idx > 0 ? url.substring(0, idx) : url;
    }
}
