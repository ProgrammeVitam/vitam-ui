/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import fr.gouv.vitamui.authserver.config.AuthServerProperties;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.cas.CreateTokenRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.CreateTokenResponseDto;
import fr.gouv.vitamui.iam.common.dto.cas.HrdEntryDto;
import fr.gouv.vitamui.iam.common.dto.cas.JitProvisionRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateResponseDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Thin HTTP client to IAM's CAS endpoints. Used by the Spring Authorization Server to :
 * - resolve HRD entries for an email (mini HRD),
 * - validate a user's password (delegated login),
 * - mint an opaque access token ({@code TOK-<UUID>}) persisted in the {@code tokens} collection,
 * - resolve / JIT-provision users after external OIDC or SAML federation.
 *
 * <p>The channel to IAM is authenticated via mTLS when a Spring {@code SslBundle} is configured
 * (property {@code vitamui.auth-server.iam.ssl-bundle}). The client keystore identifies this
 * auth-server to IAM (CN reused as system principal on the IAM side); the truststore validates
 * IAM's server certificate. Falls back to a trust-all mode (dev only) when no bundle is set.
 */
@Component
public class IamClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(IamClient.class);

    private final RestClient restClient;

    public IamClient(AuthServerProperties properties, SslBundles sslBundles) {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getIam().getBaseUrl());
        String bundleName = properties.getIam().getSslBundle();
        if (bundleName != null && !bundleName.isBlank()) {
            SslBundle bundle = sslBundles.getBundle(bundleName);
            builder = builder.requestFactory(
                new HttpComponentsClientHttpRequestFactory(
                    mtlsHttpClient(bundle, properties.getIam().isDisableHostnameVerification())
                )
            );
            LOGGER.info("IAM client uses mTLS via SslBundle '{}'", bundleName);
        } else if (properties.getIam().isTrustAllCerts()) {
            builder = builder.requestFactory(new HttpComponentsClientHttpRequestFactory(trustAllHttpClient()));
            LOGGER.warn("IAM client uses trust-all-certs mode — DO NOT USE OUTSIDE DEV");
        }
        // Attach the identity headers IAM's pre-auth filter expects on every call — without X-Origin
        // buildFromExternalRequest throws, and X-Application-Id / X-Request-Id feed the logbook.
        String applicationId = properties.getIam().getInternalApplicationId();
        builder = builder.requestInterceptor((request, body, execution) -> {
            request.getHeaders().set("X-Origin", "INTERNAL");
            request.getHeaders().set("X-Application-Id", applicationId != null ? applicationId : "auth-server");
            request.getHeaders().set("X-Request-Id", java.util.UUID.randomUUID().toString());
            // /cas/* endpoints are cross-tenant service endpoints; -1 mirrors the fallback IAM applies
            // to whitelisted paths and keeps HttpContext.getTenantIdentifier happy without leaking a
            // real tenant id into this system-to-system call.
            request.getHeaders().set("X-Tenant-Id", "-1");
            return execution.execute(request, body);
        });
        this.restClient = builder.build();
    }

    private static CloseableHttpClient mtlsHttpClient(SslBundle bundle, boolean disableHostnameVerification) {
        SSLContext sslContext = bundle.createSslContext();
        HostnameVerifier verifier = disableHostnameVerification
            ? NoopHostnameVerifier.INSTANCE
            : new DefaultHostnameVerifier();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, verifier);
        return HttpClients.custom()
            .setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslSocketFactory).build()
            )
            .build();
    }

    private static CloseableHttpClient trustAllHttpClient() {
        try {
            SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                .build();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE
            );
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .build();
            return HttpClients.custom()
                .setConnectionManager(
                    PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslSocketFactory).build()
                )
                .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build trust-all HTTP client", e);
        }
    }

    public List<HrdEntryDto> resolveHrd(String email) {
        return restClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(RestApi.V1_CAS_URL + RestApi.CAS_HRD_PATH).queryParam("email", email).build())
            .retrieve()
            .body(new ParameterizedTypeReference<List<HrdEntryDto>>() {});
    }

    /**
     * Delegates password validation to IAM. Returns the {@link UserDto} if authenticated, or {@code null}
     * on {@code 401 UNAUTHORIZED} (bad credentials). Any other error is propagated.
     */
    public UserDto login(String email, String password, String customerId) {
        LoginRequestDto request = new LoginRequestDto();
        request.setLoginEmail(email);
        request.setLoginCustomerId(customerId);
        request.setPassword(password);
        return restClient
            .post()
            .uri(RestApi.V1_CAS_URL + RestApi.CAS_LOGIN_PATH)
            .body(request)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                throw new BadCredentialsClientException("IAM returned " + resp.getStatusCode());
            })
            .body(UserDto.class);
    }

    public String createOpaqueAuthToken(String userId, boolean surrogation, boolean api) {
        CreateTokenResponseDto response = restClient
            .post()
            .uri(RestApi.V1_CAS_URL + RestApi.CAS_TOKENS_PATH)
            .body(new CreateTokenRequestDto(userId, surrogation, api))
            .retrieve()
            .body(CreateTokenResponseDto.class);
        return response != null ? response.getTokenId() : null;
    }

    /**
     * Verifies that an ACCEPTED subrogation exists between the super-user and surrogate and returns
     * both resolved user ids. Returns {@code null} on 404 (no matching subrogation).
     */
    public SubrogationValidateResponseDto validateSubrogation(SubrogationValidateRequestDto request) {
        return restClient
            .post()
            .uri(RestApi.V1_CAS_URL + RestApi.CAS_SUBROGATION_VALIDATE_PATH)
            .body(request)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                throw new SubrogationNotValidException("IAM returned " + resp.getStatusCode());
            })
            .body(SubrogationValidateResponseDto.class);
    }

    /** Marker exception mapped to a client-side error upstream. */
    public static class SubrogationNotValidException extends RuntimeException {
        public SubrogationNotValidException(String message) {
            super(message);
        }
    }

    /**
     * Loads the full {@link IdentityProviderDto} for the given identity provider id.
     * Used by the federated authentication path to build the {@code ClientRegistration}
     * at each callback. Returns {@code null} on 404.
     */
    public IdentityProviderDto getIdentityProvider(String id) {
        return restClient
            .get()
            .uri(RestApi.V1_CAS_URL + RestApi.CAS_IDP_PATH + "/" + id)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                throw new IdentityProviderNotFoundException("IAM returned " + resp.getStatusCode());
            })
            .body(IdentityProviderDto.class);
    }

    /**
     * Resolves the vitam-ui user matching an identity received from an external IdP (via OIDC or SAML),
     * with auto-provisioning if the provider has it enabled. Returns {@code null} if IAM returns 404.
     */
    public fr.gouv.vitamui.commons.api.domain.UserDto resolveExternalUser(
        String loginEmail,
        String loginCustomerId,
        String idpId,
        String userIdentifier
    ) {
        LOGGER.info("resolveExternalUser call email={} customer={} idp={}", loginEmail, loginCustomerId, idpId);
        try {
            fr.gouv.vitamui.commons.api.domain.UserDto user = restClient
                .get()
                .uri(uriBuilder ->
                    uriBuilder
                        .path(RestApi.V1_CAS_URL + RestApi.CAS_USERS_PATH + RestApi.USERS_PROVISIONING)
                        .queryParam("loginEmail", loginEmail)
                        .queryParam("loginCustomerId", loginCustomerId)
                        .queryParamIfPresent("idp", java.util.Optional.ofNullable(idpId))
                        .queryParamIfPresent("userIdentifier", java.util.Optional.ofNullable(userIdentifier))
                        .build()
                )
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    // Absorb 404 explicitly and abort the body-mapping — we return null via the catch below.
                    throw new UserNotFoundResponseException();
                })
                .body(fr.gouv.vitamui.commons.api.domain.UserDto.class);
            LOGGER.info("resolveExternalUser email={} → id={}", loginEmail, user != null ? user.getId() : "null");
            return user;
        } catch (UserNotFoundResponseException e) {
            LOGGER.info("resolveExternalUser email={} → 404 (not found)", loginEmail);
            return null;
        } catch (Exception e) {
            LOGGER.warn("resolveExternalUser email={} → error: {}", loginEmail, e.toString());
            throw e;
        }
    }

    private static final class UserNotFoundResponseException extends RuntimeException {}

    public static class IdentityProviderNotFoundException extends RuntimeException {
        public IdentityProviderNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Just-in-Time provisions a new vitam-ui user in IAM from the claims received via an external IdP.
     * Requires the IdentityProvider to have {@code autoProvisioningEnabled=true} and {@code defaultGroupId} set.
     */
    public fr.gouv.vitamui.commons.api.domain.UserDto jitProvisionUser(JitProvisionRequestDto request) {
        return restClient
            .post()
            .uri(RestApi.V1_CAS_URL + RestApi.CAS_USERS_JIT_PATH)
            .body(request)
            .retrieve()
            .body(fr.gouv.vitamui.commons.api.domain.UserDto.class);
    }

    /** Marker exception mapped to {@link org.springframework.security.authentication.BadCredentialsException} upstream. */
    public static class BadCredentialsClientException extends RuntimeException {
        public BadCredentialsClientException(String message) {
            super(message);
        }
    }
}
