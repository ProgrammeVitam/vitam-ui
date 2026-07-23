/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for the auth-server. Allows the vitam-ui Angular clients (running on a different
 * origin, e.g. {@code https://dev.vitamui.com:4200}) to consume the OIDC endpoints (discovery, token,
 * jwks, userinfo) and the login JSON API.
 */
@Configuration
public class CorsConfig {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource(AuthServerProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(properties.getCors().getAllowedOrigins());
        config.setAllowedMethods(
            List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.OPTIONS.name(), HttpMethod.HEAD.name())
        );
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/.well-known/**", config);
        source.registerCorsConfiguration("/oauth2/**", config);
        source.registerCorsConfiguration("/userinfo", config);
        source.registerCorsConfiguration("/connect/**", config);
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
