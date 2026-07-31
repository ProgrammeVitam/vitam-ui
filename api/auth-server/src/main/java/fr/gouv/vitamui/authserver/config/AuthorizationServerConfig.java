/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import fr.gouv.vitamui.authserver.security.FederatedLoginSuccessHandler;
import fr.gouv.vitamui.authserver.security.IamAuthenticationProvider;
import fr.gouv.vitamui.authserver.security.IamClient;
import fr.gouv.vitamui.authserver.security.IamRevocationLogoutSuccessHandler;
import fr.gouv.vitamui.authserver.security.MongoClientRegistrationRepository;
import fr.gouv.vitamui.authserver.security.OpaqueVitamTokenGenerator;
import fr.gouv.vitamui.authserver.security.PublicClientRevocationAuthenticationConverter;
import fr.gouv.vitamui.authserver.security.PublicClientRevocationAuthenticationProvider;
import fr.gouv.vitamui.authserver.security.SubrogationConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(AuthServerProperties.class)
public class AuthorizationServerConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        // Shared between both filter chains so the Authentication persisted by the SPA login flow (Chain 2)
        // is picked up by the OAuth2 authorize endpoint (Chain 1) — that's what makes SSO work when a
        // second client (e.g. identity :4201) hits /oauth2/authorize after the user already logged in.
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
        HttpSecurity http,
        CorsConfigurationSource corsConfigurationSource,
        RegisteredClientRepository registeredClientRepository,
        SecurityContextRepository securityContextRepository,
        IamClient iamClient
    ) throws Exception {
        http.oauth2AuthorizationServer(authorizationServer -> {
            http.securityMatcher(authorizationServer.getEndpointsMatcher());
            authorizationServer.oidc(oidc ->
                oidc.logoutEndpoint(logout -> {
                    var tolerantProvider =
                        new fr.gouv.vitamui.authserver.security.SubrogationTolerantOidcLogoutAuthenticationProvider(
                            registeredClientRepository,
                            () ->
                                http.getSharedObject(
                                    org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService.class
                                )
                        );
                    // Replace the default OidcLogoutAuthenticationProvider entirely — it enforces a strict
                    // sub check that fails in the legitimate subrogation transition. Our tolerant provider
                    // handles both the normal and subrogated cases.
                    logout.authenticationProviders(providers -> {
                        providers.removeIf(p ->
                            p instanceof
                            org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationProvider
                        );
                        providers.add(0, tolerantProvider);
                    });
                    // Propagate end-of-session to the other vitam-ui apps: after SAS accepts the logout,
                    // ask IAM to purge every opaque TOK-<UUID> pointing to the user (sub of the id_token).
                    logout.logoutResponseHandler(new IamRevocationLogoutSuccessHandler(iamClient));
                })
            );
            // Allow public (PKCE) clients to hit /oauth2/revoke with just client_id — RFC 7009 permits this,
            // SAS enforces client auth by default.
            authorizationServer.clientAuthentication(clientAuth -> {
                clientAuth.authenticationConverter(
                    new PublicClientRevocationAuthenticationConverter(registeredClientRepository)
                );
                clientAuth.authenticationProvider(
                    new PublicClientRevocationAuthenticationProvider(registeredClientRepository)
                );
            });
        });

        http.cors(cors -> cors.configurationSource(corsConfigurationSource));
        http.securityContext(sc -> sc.securityContextRepository(securityContextRepository));
        // Keep the same JSESSIONID across /oauth2/authorize requests: without this, Spring's default
        // "changeSessionId" fixation strategy rotates the cookie during the flow and the browser's second
        // /oauth2/authorize (from another client, e.g. identity) presents a stale id → new session → SSO KO.
        http.sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).sessionFixation(sf -> sf.none())
        );
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        MediaTypeRequestMatcher htmlRequests = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
        htmlRequests.setIgnoredMediaTypes(Set.of(MediaType.ALL));

        http.exceptionHandling(exceptions ->
            exceptions.defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), htmlRequests)
        );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(
        HttpSecurity http,
        CorsConfigurationSource corsConfigurationSource,
        SecurityContextRepository securityContextRepository,
        MongoClientRegistrationRepository clientRegistrationRepository,
        fr.gouv.vitamui.authserver.security.MongoRelyingPartyRegistrationRepository relyingPartyRegistrationRepository,
        IamClient iamClient
    ) throws Exception {
        FederatedLoginSuccessHandler federatedSuccessHandler = new FederatedLoginSuccessHandler(
            iamClient,
            securityContextRepository
        );
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).sessionFixation(sf -> sf.none())
            )
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers(
                        "/login",
                        "/login/**",
                        "/api/login/**",
                        "/oauth2/authorization/**",
                        "/login/oauth2/code/**",
                        "/saml2/authenticate/**",
                        "/login/saml2/sso/**",
                        "/saml2/service-provider-metadata/**",
                        // Static assets for the password self-service SPA (change + reset later on).
                        "/change-password",
                        "/change-password/**",
                        "/assets/**",
                        "/favicon.ico",
                        "/error"
                    )
                    .permitAll()
                    // JSON APIs for password self-service — the controller enforces auth via SecurityContext
                    // and returns 403 when unauthenticated, so the SPA can surface a clean error.
                    .requestMatchers("/api/password/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/login/**", "/api/password/**", "/login/saml2/sso/**"))
            .formLogin(form -> form.loginPage("/login").permitAll())
            .oauth2Login(oauth2 ->
                oauth2
                    .clientRegistrationRepository(clientRegistrationRepository)
                    .loginPage("/login")
                    .successHandler(federatedSuccessHandler)
                    .failureUrl("/login?error=federation")
            )
            .saml2Login(saml2 ->
                saml2
                    .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository)
                    .loginPage("/login")
                    .successHandler(federatedSuccessHandler)
                    .failureUrl("/login?error=federation")
            )
            .saml2Metadata(org.springframework.security.config.Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(AuthServerProperties properties) {
        return AuthorizationServerSettings.builder().issuer(properties.getIssuer()).build();
    }

    @Bean
    public AuthenticationManager authenticationManager(IamAuthenticationProvider iamAuthenticationProvider) {
        return new ProviderManager(iamAuthenticationProvider);
    }

    /**
     * Persistent replacement for {@code InMemoryOAuth2AuthorizationService}. Emissions survive SAS
     * restarts, which is what lets the OIDC logout endpoint keep resolving the {@code id_token} → user
     * link even when tokens outlive the process — SAS auto-picks this bean and skips its in-memory
     * default.
     */
    @Bean
    public org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService oauth2AuthorizationService(
        fr.gouv.vitamui.authserver.security.OAuth2AuthorizationDocumentRepository documents,
        RegisteredClientRepository registeredClientRepository
    ) {
        return new fr.gouv.vitamui.authserver.security.MongoOAuth2AuthorizationService(documents, registeredClientRepository);
    }

    @Bean
    public NimbusJwtEncoder jwtEncoder(@Qualifier("vitamJwkSource") JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(@Qualifier("vitamRsaPublicKey") RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Composite token generator: opaque access token (persisted in Mongo via IAM) + JWT id_token (OIDC) + refresh token.
     * Order matters: {@link OpaqueVitamTokenGenerator} is consulted first for access_token; it returns {@code null}
     * for other token types, letting {@link JwtGenerator} emit the id_token.
     * <p>
     * Must be the ONLY {@link OAuth2TokenGenerator} bean in the context — SAS uses
     * {@code getBeanProvider(...).getIfUnique()} which returns {@code null} when several beans exist and falls back
     * to a default composite that emits base64url opaque tokens (not our {@code TOK-<UUID>} format).
     */
    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator(
        NimbusJwtEncoder jwtEncoder,
        IamClient iamClient,
        AuthServerProperties properties,
        OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer
    ) {
        OpaqueVitamTokenGenerator opaqueVitamTokenGenerator = new OpaqueVitamTokenGenerator(iamClient, properties);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtCustomizer);
        OAuth2AccessTokenGenerator fallbackAccessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(
            opaqueVitamTokenGenerator,
            jwtGenerator,
            fallbackAccessTokenGenerator,
            refreshTokenGenerator
        );
    }

    /**
     * Ensures the {@code sub} claim of the OIDC {@code id_token} carries the vitam-ui user id (Mongo id),
     * not the giant Lombok {@code UserDto.toString()} output. Also flags subrogation via a custom claim to
     * ease debugging.
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return (JwtEncodingContext context) -> {
            Object principal = context.getPrincipal() != null ? context.getPrincipal().getPrincipal() : null;
            UserDto user = null;
            if (principal instanceof fr.gouv.vitamui.authserver.security.VitamuiPrincipal vp) {
                user = vp.getUserDto();
            } else if (principal instanceof UserDto direct) {
                user = direct;
            }
            if (user != null) {
                if (user.getId() != null) {
                    context.getClaims().subject(user.getId());
                }
                if (user.getEmail() != null) {
                    context.getClaims().claim("email", user.getEmail());
                }
            }
            boolean subrogated =
                context.getPrincipal() != null &&
                context
                    .getPrincipal()
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> SubrogationConstants.SUBROGATED_AUTHORITY.equals(a.getAuthority()));
            if (subrogated) {
                context.getClaims().claim("surrogation", true);
            }
        };
    }
}
