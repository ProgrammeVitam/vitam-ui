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
package fr.gouv.vitamui.commons.security.client.config;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import fr.gouv.vitamui.commons.security.client.util.TrustedHttpURLConnectionFactory;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.validation.constraints.NotNull;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Base CAS security configurer.
 *
 *
 */
public abstract class BaseCasSecurityConfigurer extends WebSecurityConfigurerAdapter {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(BaseCasSecurityConfigurer.class);

    private static final String LOGIN_ENDPOINT = "login";

    private static final String LOGOUT_ENDPOINT = "logout";

    private static final String CALLBACK_ENDPOINT = "/callback";

    @Value("${cas.external-url}")
    @NotNull
    private String casExternalUrl;

    @Value("${cas.internal-url}")
    @NotNull
    private String casInternalUrl;

    @Value("${server.host}")
    @NotNull
    private String host;

    @Value("${server.port}")
    @NotNull
    private int port;

    @Value("${server.scheme:https}")
    private String scheme;

    @Value("${cas.ssl.trust-store:}")
    @NotNull
    private String casTrustStore;

    @Value("${cas.ssl.trust-store-password:}")
    @NotNull
    private String casTrustStorePassword;

    @Value("${cas.ssl.trust-store-type:JKS}")
    @NotNull
    private String casTrustStoreType;

    @Value("${cas.ssl.hostname-verification:true}")
    protected Boolean hostnameVerification;

    @Value("${cas.callback-url}")
    @NotNull
    private String casCallbackUrl;

    @Value("${ui-prefix}")
    @NotNull
    private String uiPrefix;

    @Bean
    public CasLogoutUrl casLogoutUrl() {
        final String casLogoutUrl;
        if (casExternalUrl.endsWith("/")) {
            casLogoutUrl = casExternalUrl + LOGOUT_ENDPOINT;
        } else {
            casLogoutUrl = casExternalUrl + "/" + LOGOUT_ENDPOINT;
        }

        return new CasLogoutUrl(casLogoutUrl);
    }

    @Bean
    protected ServiceProperties serviceProperties() {
        final ServiceProperties sp = new ServiceProperties();
        sp.setService(casCallbackUrl);
        return sp;
    }

    @Bean
    protected Cas30ServiceTicketValidator cas30ServiceTicketValidator() {
        try {

            SSLSocketFactory sslSocketFactory = null;

            // Use given truststore if configured
            if (!casTrustStore.isEmpty()) {
                final KeyStore truststore = KeyStore.getInstance(casTrustStoreType);
                try (final InputStream is = new FileInputStream(casTrustStore)) {
                    truststore.load(is, casTrustStorePassword.toCharArray());
                }
                final TrustManagerFactory tmf = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(truststore);
                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
                sslSocketFactory = sslContext.getSocketFactory();
            }

            // Use default configuration truststore
            if (sslSocketFactory == null) {
                sslSocketFactory = SSLContext.getDefault().getSocketFactory();
            }
            final HostnameVerifier hostnameVerifier = hostnameVerification ? null : TrustAllHostnameVerifier.INSTANCE;
            final Cas30ServiceTicketValidator validator = new Cas30ServiceTicketValidator(casInternalUrl);
            validator.setURLConnectionFactory(new TrustedHttpURLConnectionFactory(hostnameVerifier, sslSocketFactory));
            return validator;
        }
        catch (final Exception e) {
            throw new InternalServerException("Cannot create SSLSocketFactory", e);
        }
    }

    @Bean
    protected CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        final CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setFilterProcessesUrl("/" + uiPrefix + CALLBACK_ENDPOINT);
        casAuthenticationFilter.setAuthenticationManager(authenticationManager());
        casAuthenticationFilter.setSessionAuthenticationStrategy(new SessionFixationProtectionStrategy());
        casAuthenticationFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler(casExternalUrl));
        return casAuthenticationFilter;
    }

    @Bean
    protected VitamUICasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        final String casLoginUrl;
        if (casExternalUrl.endsWith("/")) {
            casLoginUrl = casExternalUrl + LOGIN_ENDPOINT;
        } else {
            casLoginUrl = casExternalUrl + "/" + LOGIN_ENDPOINT;
        }

        final VitamUICasAuthenticationEntryPoint casAuthenticationEntryPoint = new VitamUICasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(casLoginUrl);
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Bean
    protected CasAuthenticationProvider casAuthenticationProvider() {
        final CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(customUserDetailsService());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(cas30ServiceTicketValidator());
        casAuthenticationProvider.setKey(host);
        return casAuthenticationProvider;
    }

    @Bean
    protected AuthenticationUserDetailsService<CasAssertionAuthenticationToken> customUserDetailsService() {
        return token -> {
            final AttributePrincipal principal = token.getAssertion().getPrincipal();
            final AuthUserDto user = new AuthUserDto(principal.getName(),
                    principal.getAttributes());
            LOGGER.debug("user: {}", user);
            return user;
        };
    }
}
