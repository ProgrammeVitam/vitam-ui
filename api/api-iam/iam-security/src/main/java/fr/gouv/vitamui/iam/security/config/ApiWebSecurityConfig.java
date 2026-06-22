/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 * contact@programmevitam.fr
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
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
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.iam.security.config;

import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.security.filter.RequestHeadersAuthenticationFilter;
import fr.gouv.vitamui.iam.security.filter.TenantHeaderFilter;
import fr.gouv.vitamui.iam.security.filter.TokenExtractor;
import fr.gouv.vitamui.iam.security.filter.X509CertificateExtractor;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_TOKEN_HEADER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * The security configuration.
 */
@Getter
@Setter
public abstract class ApiWebSecurityConfig {

    private static final String GATEWAY_ENABLED = "gateway.enabled";
    private static final String CAS_TENANT_IDENTIFIER = "cas.tenant.identifier";

    private static final String CLIENT_CERTIFICATE_HEADER_NAME = "server.ssl.client-certificate-header-name";

    protected AuthenticationProvider apiAuthenticationProvider;
    protected RestExceptionHandler restExceptionHandler;
    protected SecurityService securityService;
    protected Environment env;
    private final boolean isGatewayEnabled;
    private final Integer casTenantIdentifier;

    public ApiWebSecurityConfig(
        final AuthenticationProvider apiAuthenticationProvider,
        final RestExceptionHandler restExceptionHandler,
        final SecurityService securityService,
        final Environment env
    ) {
        this.apiAuthenticationProvider = apiAuthenticationProvider;
        this.restExceptionHandler = restExceptionHandler;
        this.securityService = securityService;
        this.env = env;

        this.isGatewayEnabled = env.getProperty(GATEWAY_ENABLED, Boolean.class, false);
        this.casTenantIdentifier = env.getProperty(CAS_TENANT_IDENTIFIER, Integer.class, -1);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
        throws Exception {
        // @formatter:off
        return http
            .authorizeHttpRequests(
                requests -> requests
                    .requestMatchers(getAuthList())
                    .permitAll())
            .authorizeHttpRequests(requests -> requests
                .anyRequest()
                .authenticated())
            .cors(cors -> cors.configurationSource(this::getCorsConfiguration))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(getUnauthorizedHandler()))
            .csrf(CsrfConfigurer::disable)
            .addFilterAt(getRequestHeadersAuthenticationFilter(authenticationManager), BasicAuthenticationFilter.class)
            .addFilterAt(getTenantHeaderFilter(), BasicAuthenticationFilter.class)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(apiAuthenticationProvider)
            .build();
        // @formatter:on
    }

    private CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        var corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        var methodsAllowed = List.of(GET.name(), POST.name(), HEAD.name(), PATCH.name(), PUT.name(), DELETE.name());
        corsConfiguration.setAllowedMethods(methodsAllowed);
        return corsConfiguration;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(getAuthList());
    }

    protected String[] getAuthList() {
        return new String[] {
            "/error**",
            "/favicon.ico",
            "/actuator/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs/**",
            "/webjars/**",
            "/v1/*/*/*/signed-download/*",
        };
    }

    @Bean
    public AuthenticationManager getAuthenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected ApiAuthenticationEntryPoint getUnauthorizedHandler() {
        return new ApiAuthenticationEntryPoint(restExceptionHandler);
    }

    protected AbstractPreAuthenticatedProcessingFilter getRequestHeadersAuthenticationFilter(
        AuthenticationManager authenticationManager
    ) throws Exception {
        return new RequestHeadersAuthenticationFilter(
            authenticationManager,
            getX509CertificateExtractors(),
            getTokenExtractors()
        );
    }

    protected TenantHeaderFilter getTenantHeaderFilter() {
        return new TenantHeaderFilter(securityService, casTenantIdentifier);
    }

    // This is a temporary patch to allow mTLS authentication behind reverse proxy or full mTLS during migration
    private List<X509CertificateExtractor> getX509CertificateExtractors() {
        final List<X509CertificateExtractor> x509CertificateExtractors = new ArrayList<>();
        x509CertificateExtractors.add(X509CertificateExtractor.requestAttributeX509CertificateExtractor());
        if (isGatewayEnabled) {
            final String certificateHeaderName = env.getProperty(CLIENT_CERTIFICATE_HEADER_NAME);
            x509CertificateExtractors.add(
                X509CertificateExtractor.requestHeaderX509CertificateExtractor(certificateHeaderName)
            );
        }
        return x509CertificateExtractors;
    }

    // This is a temporary patch to get authentication token when service is behind a gateway during migration
    private List<TokenExtractor> getTokenExtractors() {
        final List<TokenExtractor> tokenExtractors = new ArrayList<>();
        tokenExtractors.add(TokenExtractor.headerExtractor(X_USER_TOKEN_HEADER));
        if (isGatewayEnabled) {
            tokenExtractors.add(TokenExtractor.bearerExtractor());
        }
        return tokenExtractors;
    }
}
