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
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import fr.gouv.vitamui.authserver.security.IamAuthenticationProvider;
import fr.gouv.vitamui.authserver.security.IamClient;
import fr.gouv.vitamui.authserver.security.OpaqueVitamTokenGenerator;
import fr.gouv.vitamui.authserver.security.PublicClientRevocationAuthenticationConverter;
import fr.gouv.vitamui.authserver.security.PublicClientRevocationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(AuthServerProperties.class)
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
        HttpSecurity http,
        CorsConfigurationSource corsConfigurationSource,
        RegisteredClientRepository registeredClientRepository
    ) throws Exception {
        http.oauth2AuthorizationServer(authorizationServer -> {
            http.securityMatcher(authorizationServer.getEndpointsMatcher());
            authorizationServer.oidc(Customizer.withDefaults());
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
        CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/login", "/login/**", "/api/login/**", "/assets/**", "/favicon.ico", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/login/**"))
            .formLogin(form -> form.loginPage("/login").permitAll());

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
        AuthServerProperties properties
    ) {
        OpaqueVitamTokenGenerator opaqueVitamTokenGenerator = new OpaqueVitamTokenGenerator(iamClient, properties);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        OAuth2AccessTokenGenerator fallbackAccessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(
            opaqueVitamTokenGenerator,
            jwtGenerator,
            fallbackAccessTokenGenerator,
            refreshTokenGenerator
        );
    }
}
