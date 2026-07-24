/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import fr.gouv.vitamui.authserver.config.AuthServerProperties;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.iam.common.dto.cas.CreateTokenRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.CreateTokenResponseDto;
import fr.gouv.vitamui.iam.common.dto.cas.HrdEntryDto;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateResponseDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Thin HTTP client to IAM's CAS endpoints. Used by the POC Spring Authorization Server to :
 * - resolve HRD entries for an email (mini HRD),
 * - validate a user's password (delegated login),
 * - mint an opaque access token ({@code TOK-<UUID>}) persisted in the {@code tokens} collection.
 *
 * <p>Security note: the POC calls IAM without an auth header on the new endpoints (see
 * {@code CasController#createAuthToken} / {@code resolveHrd}). Production hardening (mTLS or signed
 * inter-service headers) is deferred to Phase 2.
 */
@Component
public class IamClient {

    private final RestClient restClient;

    public IamClient(AuthServerProperties properties) {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getIam().getBaseUrl());
        if (properties.getIam().isTrustAllCerts()) {
            builder = builder.requestFactory(new HttpComponentsClientHttpRequestFactory(trustAllHttpClient()));
        }
        this.restClient = builder.build();
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

    /** Marker exception mapped to {@link org.springframework.security.authentication.BadCredentialsException} upstream. */
    public static class BadCredentialsClientException extends RuntimeException {
        public BadCredentialsClientException(String message) {
            super(message);
        }
    }
}
