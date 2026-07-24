/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Dynamic {@link RelyingPartyRegistrationRepository} that fetches SAML identity providers from IAM on
 * demand — a single {@link IdentityProviderDto} per {@code registrationId} (which is the Mongo id of
 * the IdP). Analogue of {@link MongoClientRegistrationRepository} for the SAML world.
 *
 * <p>The IAM lookup is expensive (network call + PII + private keys in the response), so results are
 * cached in Caffeine with a 60 s TTL — same cadence as {@link MongoClientRegistrationRepository}.
 *
 * <p>{@code null} is returned when the id doesn't correspond to a SAML provider — Spring's SAML2 login
 * filter treats it as "unknown registration".
 */
@Component
public class MongoRelyingPartyRegistrationRepository implements RelyingPartyRegistrationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoRelyingPartyRegistrationRepository.class);
    // The IdP DTO carries either "SAML" (legacy CAS wording) or "SAML_RESPONSE" (Spring authorities
     // constant) depending on where the entity was created — accept both.
    private static final java.util.Set<String> SAML_PROTOCOL_ALIASES = java.util.Set.of("SAML", "SAML_RESPONSE");

    private final IamClient iamClient;
    private final Cache<String, Optional<RelyingPartyRegistration>> cache;

    public MongoRelyingPartyRegistrationRepository(IamClient iamClient) {
        this.iamClient = iamClient;
        this.cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(100).build();
    }

    @Override
    public RelyingPartyRegistration findByRegistrationId(String registrationId) {
        return cache.get(registrationId, this::load).orElse(null);
    }

    private Optional<RelyingPartyRegistration> load(String registrationId) {
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
        if (idp.getProtocoleType() == null
            || !SAML_PROTOCOL_ALIASES.contains(idp.getProtocoleType().toUpperCase())) {
            LOGGER.debug("IdP {} is not a SAML provider (protocoleType={})", registrationId, idp.getProtocoleType());
            return Optional.empty();
        }
        if (!StringUtils.hasText(idp.getIdpMetadata())) {
            LOGGER.warn("IdP {} misses idpMetadata XML — skipping", registrationId);
            return Optional.empty();
        }
        if (!StringUtils.hasText(idp.getKeystoreBase64())) {
            LOGGER.warn("IdP {} misses keystoreBase64 — skipping (SP signing/decryption not available)", registrationId);
            return Optional.empty();
        }

        try {
            Saml2X509Credential spCredential = buildSpCredential(idp);
            RelyingPartyRegistration registration = RelyingPartyRegistrations
                // Parse asserting-party (IdP) metadata XML: SSO URL, entityId, verification cert.
                .fromMetadata(new ByteArrayInputStream(idp.getIdpMetadata().getBytes()))
                .registrationId(idp.getId())
                // {baseUrl} is substituted with the SAS public URL at request time.
                .entityId("{baseUrl}/saml2/service-provider-metadata/{registrationId}")
                .assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                .signingX509Credentials(c -> c.add(spCredential))
                .decryptionX509Credentials(c -> c.add(spCredential))
                .build();
            LOGGER.info(
                "Loaded SAML RelyingPartyRegistration for IdP {} (name={}, assertingEntityId={})",
                registrationId,
                idp.getName(),
                registration.getAssertingPartyMetadata().getEntityId()
            );
            return Optional.of(registration);
        } catch (Exception e) {
            LOGGER.error("Failed to build RelyingPartyRegistration for IdP {} : {}", registrationId, e.toString(), e);
            return Optional.empty();
        }
    }

    /**
     * Parses the base64-encoded PKCS12/JKS keystore and extracts the first private-key entry as an SP
     * credential usable both for signing outgoing AuthnRequests and decrypting incoming assertions.
     */
    private Saml2X509Credential buildSpCredential(IdentityProviderDto idp) throws Exception {
        byte[] keystoreBytes = Base64.getDecoder().decode(idp.getKeystoreBase64());
        char[] keystorePassword = idp.getKeystorePassword() != null ? idp.getKeystorePassword().toCharArray() : new char[0];
        char[] privateKeyPassword = idp.getPrivateKeyPassword() != null
            ? idp.getPrivateKeyPassword().toCharArray()
            : keystorePassword;

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(keystoreBytes), keystorePassword);
        } catch (Exception e) {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ByteArrayInputStream(keystoreBytes), keystorePassword);
        }

        String alias = null;
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String candidate = aliases.nextElement();
            if (keyStore.isKeyEntry(candidate)) {
                alias = candidate;
                break;
            }
        }
        if (alias == null) {
            throw new IllegalStateException("No key entry found in keystore of IdP " + idp.getId());
        }
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, privateKeyPassword);
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
        return new Saml2X509Credential(
            privateKey,
            certificate,
            Saml2X509Credential.Saml2X509CredentialType.SIGNING,
            Saml2X509Credential.Saml2X509CredentialType.DECRYPTION
        );
    }
}
